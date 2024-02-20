package com.UmiUni.shop.service;

import com.UmiUni.shop.dto.ProductDTO;
import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.model.ProductWithAttributes;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product getProduct(Long id);
    List<Product> getAllProducts();
    Product updateProduct(Long id, Product productDetails);
    void deleteProduct(Long id);

    ProductWithAttributes getProductWithAttributes(Long productId);

    Product findBySkuCode(String skuCode);

    public void reduceProductInventory(String skuCode, int quantity);

    public void lockInventory(String skuCode, int quantity);

    Product updateProductAndImages(Long productId, String productStr, MultipartFile[] newImages, List<Long> imagesToDelete) throws JsonProcessingException;

    Page<ProductDTO> getProductsByPage(int page, int size);

    void deleteProductImageById(Long productId, Long imgId);
}
