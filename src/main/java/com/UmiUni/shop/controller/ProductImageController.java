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
import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    // upload image to AWS S3
    @PostMapping("/upload/s3")
    // http://localhost:9001/api/v1/products/1/images/upload/s3
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

    // get img by cache from AWS S3
    // http://localhost:9001/api/v1/products/78/images/main/s3/img/90
    @GetMapping("/main/s3/img/{id}")
    public ResponseEntity<byte[]> getImageFromAWSByCache(@PathVariable Long id, @PathVariable String productId) {
        byte[] imageData = productImageService.getImageFromAWSByCache(id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 根据实际情况动态确定内容类型
                .body(imageData);
    }

    @GetMapping("/main/img/{id}")
    public ResponseEntity<byte[]> getImageByCache(@PathVariable Long id) {
        byte[] imageData = productImageService.getImageDataByCache(id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 根据实际情况动态确定内容类型
                .body(imageData);
    }

    // get all images from AWS S3
    // http://localhost:9001/api/v1/products/1/images/s3/img/all
    @GetMapping("/s3/img/all")
    public ResponseEntity<List<String>> getImagesByProductIdFromAWS(@PathVariable Long productId) {
        try {
            List<String> imageUrls = productImageService.getImagesPathByProductIdFromAWS(productId);
            return ResponseEntity.ok(imageUrls);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // get all images from AWS S3
    // http://localhost:9001/api/v1/products/1/images/s3/img/82
    @GetMapping("/s3/img/{imgId}")
    public ResponseEntity<String> getImageByImgIdFromAWS(@PathVariable Long imgId, @PathVariable String productId) {
        try {
            String imageUrls = productImageService.getImagesByImgIdFromAWS(imgId);
            return ResponseEntity.ok(imageUrls);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
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

    // delete images from AWS S3
    @DeleteMapping("/s3/img/{imgId}")
    // http://localhost:9001/api/v1/products/1/images/s3/img/82
    public ResponseEntity<Void> deleteImageFromAWS(@PathVariable Long productId, @PathVariable Long imgId) {
        try {
            productImageService.deleteImageFromAWS(imgId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
//    @DeleteMapping("/s3/img/{fileName}")
//    public ResponseEntity<Void> deleteImageFromAWS(@PathVariable Long productId, @PathVariable String fileName) {
//        try {
//            productImageService.deleteImageFromAWS(productId, fileName);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        productImageService.deleteImage(id);
        return ResponseEntity.ok().build();
    }
}
