package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.model.ProductWithAttributes;

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
}
