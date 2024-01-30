package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.entity.ProductImage;
import com.UmiUni.shop.model.ProductWithAttributes;
import com.UmiUni.shop.service.ProductImageService;
import com.UmiUni.shop.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@Log4j2
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper is provided by Spring Boot

    // create product with images
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestParam("product") String productStr,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            Product product = objectMapper.readValue(productStr, Product.class);
            Product savedProduct = productService.createProduct(product);

            if (images != null && images.length > 0) {
                for (MultipartFile image : images) {
                    productImageService.saveImage(savedProduct.getProductId(), image);
                }
            }

            List<Long> productIds = productImageService.getImagesByProductId(product.getProductId()).stream()
                    .map(ProductImage::getId)
                    .collect(Collectors.toList());
            savedProduct.setProductImageIds(productIds);
            productService.updateProduct(product.getProductId(), savedProduct);

            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            // Handle exceptions, possibly returning an appropriate error response
            throw new RuntimeException("Error creating product", e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // update product including images
    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestParam("product") String productStr,
            @RequestParam(value = "newImages", required = false) MultipartFile[] newImages,
            @RequestParam(value = "imagesToDelete", required = false) List<Long> imagesToDelete) {
        try {
            return productService.updateProductAndImages(productId, productStr, newImages, imagesToDelete);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return ResponseEntity.ok(productService.updateProduct(id, productDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/with-attributes/{productId}")
    public ResponseEntity<ProductWithAttributes> getProductWithAttributes(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductWithAttributes(productId));
    }

    // get productInfo by skuCode
    @GetMapping("/product/{skuCode}")
    public ResponseEntity<Product> getProductBySkuCode(@PathVariable String skuCode) {
        Product product = productService.findBySkuCode(skuCode);
        return ResponseEntity.ok(product);
    }

}
