package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.ProductImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {
    ProductImage saveImage(Long productId, MultipartFile imageFile);

    ProductImage saveImageToAWS(Long productId, MultipartFile imageFile);

    ProductImage getImage(Long id);

    List<ProductImage> getImagesByProductId(Long productId);

    void deleteImage(Long id);

    byte[] getImageDataByCache(Long id);

    List<String> getImagesPathByProductIdFromAWS(Long productId);

    List<Long> getImagesIdByProductIdFromAWS(Long productId);

//    void deleteImageFromAWS(Long productId, String fileName);
    void deleteImageFromAWS(Long id);

    byte[] getImageFromAWSByCache(Long id);

    String getImagesByImgIdFromAWS(Long imgId);
}
