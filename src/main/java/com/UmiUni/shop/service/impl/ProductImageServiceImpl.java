package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.ProductImage;
import com.UmiUni.shop.repository.ProductImageRepository;
import com.UmiUni.shop.service.ProductImageService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
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

}
