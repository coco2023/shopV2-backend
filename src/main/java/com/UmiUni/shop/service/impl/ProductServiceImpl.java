package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.dto.ProductDTO;
import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.entity.ProductAttribute;
import com.UmiUni.shop.entity.ProductImage;
import com.UmiUni.shop.exception.InsufficientStockException;
import com.UmiUni.shop.exception.ProductNotFoundException;
import com.UmiUni.shop.model.ProductWithAttributes;
import com.UmiUni.shop.redis.InventoryLockService;
import com.UmiUni.shop.repository.ProductAttributeRepository;
import com.UmiUni.shop.repository.ProductRepository;
import com.UmiUni.shop.service.PaymentErrorHandlingService;
import com.UmiUni.shop.service.ProductImageService;
import com.UmiUni.shop.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    @Autowired
    private PaymentErrorHandlingService paymentErrorHandlingService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryLockService inventoryLockService;

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        log.info(product.getProductImageIds());
        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
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
        product.setProductImageIds(productDetails.getProductImageIds());

        // Save the updated product
        return productRepository.save(product);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
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

        try {
            // Check if enough locked stock is available to reduce
            if (product.getStockQuantity() == null || product.getLockedStockQuantity() < quantity) {
                throw new InsufficientStockException("Insufficient locked stock to complete the transaction");
            }
        } catch (InsufficientStockException e) {
            return;
        }

        // TODO: how to deal with the error this part?
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
    @Transactional
    public void lockInventory(String skuCode, int quantity) {

        // 尝试获取全局锁
        if (!inventoryLockService.globalLock(skuCode)) {
            throw new IllegalStateException("Unable to acquire global lock for skuCode: " + skuCode);
        }
        log.info("Able to acquire global lock for skuCode: " + skuCode);

        try {
            // 获取本地锁
            inventoryLockService.localLock(skuCode);
            log.info("Able to acquire local Lock for skuCode: " + skuCode);

            Product product = null;

            try {
                product = productRepository.findBySkuCode(skuCode)
                        .orElseThrow(() -> new ProductNotFoundException("Product not found"));  // ProductNotFoundException
                log.info(quantity + " lockInventory product Info: " + product.getSkuCode() + " " + product.getStockQuantity());

                if (product == null || product.getStockQuantity() < quantity) {
                    // 库存不足逻辑处理
                    throw new InsufficientStockException("Insufficient stock for skuCode: " + skuCode);
                }

                int availableQuantity = product.getStockQuantity() - (product.getLockedStockQuantity() != null ? -product.getLockedStockQuantity() : 0);
                log.info("availableQuantity: " + availableQuantity);

                if (availableQuantity < quantity) {
                    throw new InsufficientStockException("Insufficient available stock for product"); // InsufficientStockException
                }

                // 锁定库存
                product.setLockedStockQuantity(product.getLockedStockQuantity() + quantity);

                int newLockedQuantity = (product.getLockedStockQuantity() != null ? product.getLockedStockQuantity() : 0) + quantity;
                log.info("newLockedQuantity: " + newLockedQuantity + " " + product.getLockedStockQuantity() + " " + quantity);
                product.setLockedStockQuantity(newLockedQuantity);

                productRepository.save(product);
                log.info("lockInventory update product: " + product.getLockedStockQuantity());

            } catch (ProductNotFoundException e) {
                paymentErrorHandlingService.handleProductNotFoundException(e, product.getSkuCode(), "Product not found");
            } catch (InsufficientStockException e) {
                paymentErrorHandlingService.handleInsufficientStockException(e, product.getSkuCode(), "Insufficient available stock for product");
            } catch (Exception e) {
                paymentErrorHandlingService.handleGenericError(e, null, null);
            } finally {
                // 释放本地锁
                inventoryLockService.localUnlock(skuCode);
            }
        } finally {
            // 释放全局锁
            inventoryLockService.globalUnlock(skuCode);
        }
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProductAndImages(
            Long productId, String productStr,
            MultipartFile[] newImages, List<Long> imagesToDelete) throws JsonProcessingException {

        // Fetch the existing product
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        // Deserialize the JSON string to a Product object
        Product productDetails = objectMapper.readValue(productStr, Product.class);

        // Update product fields from productStr
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
        product.setRating(productDetails.getRating());
        product.setFinalPrice(productDetails.getFinalPrice());
        product.setDiscount(productDetails.getDiscount());

        // get image ids
        List<Long> imageIds = productDetails.getProductImageIds();

        // Delete specified images
        if (imagesToDelete != null) {
            imagesToDelete.forEach(productImageService::deleteImage);
            // remove the images in the original ids
            imagesToDelete.forEach(imageIds::remove);
        }

        // Save new images
        if (newImages != null) {
            for (MultipartFile image : newImages) {
                ProductImage productImage = productImageService.saveImage(productId, image);
                // update the new images into the original image ids
                imageIds.add(productImage.getId());
            }
        }
        product.setProductImageIds(imageIds);

        // Save the updated product
        productRepository.save(product);

        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByPage(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
//        List<Product> productList = productRepository.findAll(pageRequest).getContent();
        Page<Product> productPage = productRepository.findAll(pageRequest);

        Page<ProductDTO> response = productPage.map(this::convertToProductDTO); // 使用map转换Product到ProductDTO
        log.info("return Page<ProductDTO>: {} ", response);

        List<ProductDTO> productDTOList = productPage.getContent().stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(productDTOList, pageRequest, productPage.getTotalElements());
    }

    public ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();

        // Initialize the lazy-loaded collection
        Hibernate.initialize(product.getProductImageIds());
        // Trigger lazy loading; Force initialization
        product.getProductImageIds().size();

        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setSkuCode(product.getSkuCode());
        dto.setCategoryId(product.getCategoryId());
        dto.setCategoryName(product.getCategoryName());
        dto.setBrandId(product.getBrandId());
        dto.setBrandName(product.getBrandName());
        dto.setSupplierId(product.getSupplierId());
        dto.setSupplierName(product.getSupplierName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setFinalPrice(product.getFinalPrice());
        dto.setRating(product.getRating());
        dto.setSalesAmount(product.getSalesAmount());
        dto.setImageUrl(product.getImageUrl());
        dto.setProductImageIds(product.getProductImageIds() != null ? new ArrayList<>(product.getProductImageIds()) : null);
        dto.setStockQuantity(product.getStockQuantity());
        dto.setStockStatus(product.getStockStatus());
        dto.setShippingInfo(product.getShippingInfo());
        dto.setLastStockUpdate(product.getLastStockUpdate());
        dto.setLockedStockQuantity(product.getLockedStockQuantity());

        return dto;
    }

}
