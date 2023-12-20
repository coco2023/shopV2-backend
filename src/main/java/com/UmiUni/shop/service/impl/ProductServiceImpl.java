package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.entity.ProductAttribute;
import com.UmiUni.shop.model.ProductWithAttributes;
import com.UmiUni.shop.repository.ProductAttributeRepository;
import com.UmiUni.shop.repository.ProductRepository;
import com.UmiUni.shop.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        // Retrieve the existing product by ID
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Update the product fields with the provided details
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

        // Save the updated product
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public ProductWithAttributes getProductWithAttributes(Long productId) {
        Product product = productRepository.findById(productId)
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
    public Product findBySkuCode(String skuCode) {
        return productRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Product not found with skuCode: " + skuCode));
    }

    @Transactional
    @Override
    public void reduceProductInventory(String skuCode, int quantity) {
        Product product = productRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        log.info(quantity + " reduceProductInventory product Info: " + product.getSkuCode() + " " + product.getStockQuantity() + " " + product.getLockedStockQuantity());

        // Check if enough locked stock is available to reduce
        if (product.getStockQuantity() == null || product.getLockedStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient locked stock to complete the transaction");
        }

        // Reduce the locked stock quantity
        product.setLockedStockQuantity(product.getLockedStockQuantity() - quantity);

        // Reduce the actual stock quantity
        product.setStockQuantity(product.getStockQuantity() - quantity);

        // increase salesAmount
        product.setSalesAmount(product.getSalesAmount() + quantity);

        log.info("reduceProductInventory update product: " + product.getStockQuantity() + " " + product.getLockedStockQuantity() + " " + quantity + " " + product.getSalesAmount());

        productRepository.save(product);
    }

    @Override
    public void lockInventory(String skuCode, int quantity) {
        Product product = productRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Product not found"));  // ProductNotFoundException
        log.info(quantity + " lockInventory product Info: " + product.getSkuCode() + " " + product.getStockQuantity());

        int availableQuantity = product.getStockQuantity() - (product.getLockedStockQuantity() != null ? -product.getLockedStockQuantity() : 0);
        log.info("availableQuantity: " + availableQuantity);
        if (availableQuantity < quantity) {
            throw new RuntimeException("Insufficient available stock for product"); // InsufficientStockException
        }

        int newLockedQuantity = (product.getLockedStockQuantity() != null ? product.getLockedStockQuantity() : 0) + quantity;
        log.info("newLockedQuantity: " + newLockedQuantity + " " + product.getLockedStockQuantity() + " " + quantity);
        product.setLockedStockQuantity(newLockedQuantity);

        productRepository.save(product);

        log.info("lockInventory update product: " + product.getLockedStockQuantity());
    }
}
