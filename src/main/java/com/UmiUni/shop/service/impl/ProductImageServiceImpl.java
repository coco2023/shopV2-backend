package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.ProductImage;
import com.UmiUni.shop.repository.ProductImageRepository;
import com.UmiUni.shop.service.ProductImageService;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ProductImageServiceImpl implements ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Value("${product.image.storage.path}")
    private String storagePath;

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;
//    private AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

    @Override
    public ProductImage saveImage(Long productId, MultipartFile imageFile) {
        try {
            String fileName = UUID.randomUUID().toString()
                    + "_"
                    + imageFile.getOriginalFilename();
            Path storageDirectory = Paths.get(storagePath
                    + "/"
                    + productId);

            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }
            Path destination = storageDirectory.resolve(fileName);
            imageFile.transferTo(destination);

            ProductImage productImage = new ProductImage();
            productImage.setProductId(productId);
            productImage.setFileName(fileName);
            productImage.setFilePath(destination.toString());
            productImage.setFileSize(imageFile.getSize()); // Set the file size

            return productImageRepository.save(productImage);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image file", e);
        }
    }

    @Override
    public ProductImage saveImageToAWS(Long productId, MultipartFile imageFile) {
        try {
            // Generate a unique file name to avoid conflicts
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

            // Include the productId in the S3 object key to organize images by product
            String objectKey = productId + "/" + fileName; // Mimics a folder structure: {productId}/{fileName}

            // Metadata to set the content length and content type
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageFile.getSize());
            metadata.setContentType(imageFile.getContentType());

            // Upload the image to S3, using the objectKey that includes the productId
            s3Client.putObject(new PutObjectRequest(bucketName, objectKey, imageFile.getInputStream(), metadata));

            // Construct the S3 file URL using the objectKey
            String fileUrl = s3Client.getUrl(bucketName, objectKey).toExternalForm();

            // Create and save the ProductImage entity with S3 file URL
            ProductImage productImage = new ProductImage();
            productImage.setProductId(productId);
            productImage.setFileName(fileName);
            productImage.setFilePath(fileUrl); // Store the full URL to the S3 object
            productImage.setFileSize(imageFile.getSize()); // Set the file size

            return productImageRepository.save(productImage);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image file", e);
        }
    }

    @Override
    public String getImagesByImgIdFromAWS(Long imgId) {
        return getImage(imgId).getFilePath();
    }

    @Override
    public List<String> getImagesPathByProductIdFromAWS(Long productId) {
        List<ProductImage> productImages = getImagesByProductId(productId);

        List<String> listUrls = productImages.stream()
                .map(ProductImage::getFilePath)
                .collect(Collectors.toList());

        return listUrls;
    }

    @Override
    public List<Long> getImagesIdByProductIdFromAWS(Long productId) {
        List<Long> productImagesIds = getImagesByProductId(productId).stream()
                    .map(ProductImage::getId)
                    .collect(Collectors.toList());

        return productImagesIds;
    }

    @Override
    public ProductImage getImage(Long id) {
        return productImageRepository.findById(id).orElseThrow(() -> new RuntimeException("Image not found"));
    }

    @Override
    public List<ProductImage> getImagesByProductId(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    @Override
    public void deleteImage(Long id) {
        ProductImage image = getImage(id);
        try {
            Path fileToDelete = Paths.get(image.getFilePath());
            Files.delete(fileToDelete);
            productImageRepository.deleteById(id);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image file", e);
        }
    }

    @Override
    public byte[] getImageDataByCache(Long id) {
        String cacheKey = "image_" + id;
        // 尝试从Redis缓存中获取图片数据
        byte[] imageData = redisTemplate.opsForValue().get(cacheKey);

        if (imageData == null || imageData.length == 0) {
            // 缓存中没有找到图片数据，从文件系统中加载
            ProductImage productImage = getImage(id); // 获取ProductImage实例
            Path path = Paths.get(productImage.getFilePath());

            try {
                imageData = Files.readAllBytes(path);
                // 将图片数据存入Redis缓存
                redisTemplate.opsForValue().set(cacheKey, imageData);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read image data", e);
            }

        }
        return imageData;
    }

    @Override
    public byte[] getImageFromAWSByCache(Long id) {
        String cacheKey = "image_" + id;
        // Try to get image data from Redis cache
        byte[] imageData = redisTemplate.opsForValue().get(cacheKey);

        if (imageData == null || imageData.length == 0) {
            // Cache miss, load image data from S3
            ProductImage productImage = getImage(id); // Assume this fetches the product image details from the database
            String s3Url = productImage.getFilePath(); // Assuming the filePath stores the S3 URL

            // Assuming the filePath stores the full S3 URL
            String objectKey = extractObjectKeyFromUrl(productImage.getFilePath());

            try {
//                S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, s3Url));
                S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey));
                S3ObjectInputStream s3InputStream = s3Object.getObjectContent();
                imageData = IOUtils.toByteArray(s3InputStream); // Apache Commons IO library
                // Store the image data in Redis cache
                redisTemplate.opsForValue().set(cacheKey, imageData);
                s3InputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load image data from S3", e);
            } catch (AmazonS3Exception e) {
                log.error("Failed to fetch image from S3", e);
                throw e; // Rethrow or handle as appropriate
            }
        }
        return imageData;
    }

    @Override
    public void deleteImageFromAWS(Long id) {
        // Fetch the ProductImage entity by id
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));

        // Assuming the filePath stores the full S3 URL, extract the object key from the URL
        String objectKey = extractObjectKeyFromUrl(image.getFilePath());

        try {
            // Delete the object from the S3 bucket
            s3Client.deleteObject(bucketName, objectKey);

            // Delete the image record from the database
            productImageRepository.deleteById(id);
        } catch (AmazonServiceException e) {
            throw new RuntimeException("Failed to delete image from AWS S3", e);
        }
    }

    private String extractObjectKeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            // Remove the leading "/" and return the rest as the object key
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL: " + fileUrl, e);
        }

    }
//    @Override
//    public void deleteImageFromAWS(Long productId, String fileName) {
//        // Construct the object key using the productId and fileName
//        String objectKey = productId + "/" + fileName;
//
//        try {
//            // Delete the object from S3
//            s3Client.deleteObject(bucketName, objectKey);
//        } catch (AmazonServiceException e) {
//            throw new RuntimeException("Failed to delete image from AWS S3", e);
//        }
//    }

}

//    @Override
//    public List<String> getImagesByProductIdFromAWS(Long productId) {
//        List<String> imageUrls = new ArrayList<>();
//
//        try {
//            // Prefix to list objects within a "folder" for the specific productId
//            String prefix = productId + "/";
//
//            // List objects with the specified prefix
//            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
//                    .withBucketName(bucketName)
//                    .withPrefix(prefix);
//
//            // Iterate over the object listings
//            List<S3ObjectSummary> summaries = s3Client.listObjects(listObjectsRequest).getObjectSummaries();
//
//            for (S3ObjectSummary os : summaries) {
//                // For each object summary, generate a pre-signed URL with an expiration
//                java.util.Date expiration = new java.util.Date();
//                long expTimeMillis = expiration.getTime();
//                expTimeMillis += 1000 * 60 * 60; // URL expires in 1 hour
//                expiration.setTime(expTimeMillis);
//
//                GeneratePresignedUrlRequest generatePresignedUrlRequest =
//                        new GeneratePresignedUrlRequest(bucketName, os.getKey())
//                                .withMethod(com.amazonaws.HttpMethod.GET)
//                                .withExpiration(expiration);
//
//                URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
//
//                imageUrls.add(url.toString());
//            }
//
//            return imageUrls;
//        } catch (AmazonServiceException e) {
//            throw new RuntimeException("Failed to list images from AWS S3", e);
//        }
//    }
