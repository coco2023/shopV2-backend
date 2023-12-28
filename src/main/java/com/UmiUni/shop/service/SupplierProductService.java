package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.model.ProductWithAttributes;

import java.util.List;

public interface SupplierProductService {
    Product createProduct(Long supplierId, Product product);

    Product getProductByIdAndSupplier(Long id, Long supplierId);

    List<Product> getAllProductsBySupplier(Long supplierId);

    Product updateProductForSupplier(Long id, Long supplierId, Product productDetails);

    void deleteProductForSupplier(Long id, Long supplierId);

    ProductWithAttributes getProductWithAttributesForSupplier(Long productId, Long supplierId);

    Product getProductBySkuCodeForSupplier(String skuCode, Long supplierId);
}
