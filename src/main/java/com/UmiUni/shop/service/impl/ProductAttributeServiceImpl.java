package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.ProductAttribute;
import com.UmiUni.shop.repository.ProductAttributeRepository;
import com.UmiUni.shop.service.ProductAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductAttributeServiceImpl implements ProductAttributeService {

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    @Override
    public ProductAttribute createProductAttribute(ProductAttribute productAttribute) {
        return productAttributeRepository.save(productAttribute);
    }

    @Override
    public ProductAttribute getProductAttribute(Long id) {
        return productAttributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductAttribute not found with id: " + id));
    }

    @Override
    public List<ProductAttribute> getAllProductAttributes() {
        return productAttributeRepository.findAll();
    }

    @Override
    public ProductAttribute updateProductAttribute(Long id, ProductAttribute productAttributeDetails) {
        ProductAttribute productAttribute = getProductAttribute(id);
        productAttribute.setProductId(productAttributeDetails.getProductId());
        productAttribute.setSkuCode(productAttributeDetails.getSkuCode());
        productAttribute.setAttributeName(productAttributeDetails.getAttributeName());
        productAttribute.setAttributeValue(productAttributeDetails.getAttributeValue());
        // Other updates as needed
        return productAttributeRepository.save(productAttribute);
    }

    @Override
    public void deleteProductAttribute(Long id) {
        productAttributeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttribute> getProductAttributesByProductId(Long productId) {
        return productAttributeRepository.findByProductId(productId);
    }
}
