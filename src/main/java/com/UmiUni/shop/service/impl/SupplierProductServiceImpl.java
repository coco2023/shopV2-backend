package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.entity.ProductAttribute;
import com.UmiUni.shop.model.ProductWithAttributes;
import com.UmiUni.shop.repository.ProductAttributeRepository;
import com.UmiUni.shop.repository.ProductRepository;
import com.UmiUni.shop.service.SupplierProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierProductServiceImpl implements SupplierProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    @Override
    public Product createProduct(Long supplierId, Product product) {
        // Set the supplierId for the product
        product.setSupplierId(supplierId);
        return productRepository.save(product);
    }

    @Override
    public Product getProductByIdAndSupplier(Long id, Long supplierId) {
        return productRepository.findByProductIdAndSupplierId(id, supplierId)
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id + " and supplierId " + supplierId));  //ResourceNotFoundException
    }

    @Override
    public List<Product> getAllProductsBySupplier(Long supplierId) {
        // how about no products of suppliers
        return productRepository.findAllBySupplierId(supplierId)
                .orElseThrow(() -> new RuntimeException("Product not found with supplierId " + supplierId));
    }

    @Override
    public Product updateProductForSupplier(Long id, Long supplierId, Product productDetails) {
        Product product = productRepository.findByProductIdAndSupplierId(id, supplierId)
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id + " and supplierId " + supplierId)); //RuntimeException

        // Update fields of the existing product
        product.setProductName(productDetails.getProductName());
        product.setSkuCode(productDetails.getSkuCode());
        product.setCategoryId(productDetails.getCategoryId());
        product.setCategoryName(productDetails.getCategoryName());
        product.setBrandId(productDetails.getBrandId());
        product.setBrandName(productDetails.getBrandName());
        product.setSupplierId(productDetails.getSupplierId());
        product.setSupplierName(productDetails.getSupplierName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setImageUrl(productDetails.getImageUrl());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setStockStatus(productDetails.getStockStatus());
        product.setShippingInfo(productDetails.getShippingInfo());
        product.setLastStockUpdate(productDetails.getLastStockUpdate());

        return productRepository.save(product);
    }

    @Override
    public void deleteProductForSupplier(Long id, Long supplierId) {
        productRepository.deleteByProductIdAndSupplierId(id, supplierId);
    }

    @Override
    public ProductWithAttributes getProductWithAttributesForSupplier(Long productId, Long supplierId) {
        Product product = productRepository.findByProductIdAndSupplierId(productId, supplierId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        List<ProductAttribute> attributes = productAttributeRepository.findByProductId(productId);

        ProductWithAttributes productWithAttributes = new ProductWithAttributes();
        productWithAttributes.setProductId(product.getProductId());
        productWithAttributes.setProductName(product.getProductName());
        productWithAttributes.setSkuCode(product.getSkuCode());
        productWithAttributes.setCategoryId(product.getCategoryId());
        productWithAttributes.setCategoryName(product.getCategoryName());
        productWithAttributes.setBrandId(product.getBrandId());
        productWithAttributes.setBrandName(product.getBrandName());
        productWithAttributes.setSupplierId(product.getSupplierId());
        productWithAttributes.setSupplierName(product.getSupplierName());
        productWithAttributes.setDescription(product.getDescription());
        productWithAttributes.setPrice(product.getPrice());
        productWithAttributes.setImageUrl(product.getImageUrl());
        productWithAttributes.setStockQuantity(product.getStockQuantity());
        productWithAttributes.setStockStatus(product.getStockStatus());
        productWithAttributes.setShippingInfo(product.getShippingInfo());
        productWithAttributes.setLastStockUpdate(product.getLastStockUpdate());
        productWithAttributes.setProductAttributeList(attributes);

        return productWithAttributes;
    }

    @Override
    public Product getProductBySkuCodeForSupplier(String skuCode, Long supplierId) {
        return productRepository.findBySkuCodeAndSupplierId(skuCode, supplierId);
    }
}
