package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.ProductImage;
import com.UmiUni.shop.repository.ProductImageRepository;
import com.UmiUni.shop.service.ProductImageService;
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
