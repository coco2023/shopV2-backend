package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.ProductImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {
    ProductImage saveImage(Long productId, MultipartFile imageFile);

    ProductImage getImage(Long id);

    List<ProductImage> getImagesByProductId(Long productId);

    void deleteImage(Long id);
}
