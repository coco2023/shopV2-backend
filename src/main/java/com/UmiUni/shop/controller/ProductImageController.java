package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.ProductImage;
import com.UmiUni.shop.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    // upload image to AWS S3
    @PostMapping("/upload/s3")
    public ResponseEntity<ProductImage> uploadImageToS3(@PathVariable Long productId, @RequestParam("image") MultipartFile imageFile) {
        ProductImage productImage = productImageService.saveImageToAWS(productId, imageFile); // Call the method that uploads to S3
        return ResponseEntity.ok(productImage);
    }

    // upload image to local storage
    @PostMapping("/upload")
    public ResponseEntity<ProductImage> uploadImage(@PathVariable Long productId, @RequestParam("image") MultipartFile imageFile) {
        ProductImage productImage = productImageService.saveImage(productId, imageFile);
        return ResponseEntity.ok(productImage);
    }

    @GetMapping("/main/img/{id}")
    public ResponseEntity<byte[]> getImageByCache(@PathVariable Long id) {
        byte[] imageData = productImageService.getImageDataByCache(id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 根据实际情况动态确定内容类型
                .body(imageData);
    }

    // get images without redis cache
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        try {
            ProductImage productImage = productImageService.getImage(id);
            Path path = Paths.get(productImage.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // or determine the content type dynamically
                    .body(resource);
        } catch (MalformedURLException e) {
            // Handle exception, e.g., log an error, return a default resource, etc.
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        productImageService.deleteImage(id);
        return ResponseEntity.ok().build();
    }
}
