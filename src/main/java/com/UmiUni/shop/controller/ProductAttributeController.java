package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.ProductAttribute;
import com.UmiUni.shop.service.ProductAttributeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productAttributes")
@Api(value = "ProductAttributeController")
public class ProductAttributeController {

    @Autowired
    private ProductAttributeService productAttributeService;

    @Autowired
    private ControllerUtli controllerUtli;

    @PostMapping
    public ResponseEntity<ProductAttribute> createProductAttribute(@RequestBody ProductAttribute productAttribute) {
        return ResponseEntity.ok(productAttributeService.createProductAttribute(productAttribute));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductAttribute> getProductAttributeById(@PathVariable Long id) {
        return ResponseEntity.ok(productAttributeService.getProductAttribute(id));
    }

    @GetMapping("/all")
    public List<ProductAttribute> getAllProductAttributes() {
        return productAttributeService.getAllProductAttributes();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductAttribute> updateProductAttribute(@PathVariable Long id, @RequestBody ProductAttribute productAttributeDetails) {
        return ResponseEntity.ok(productAttributeService.updateProductAttribute(id, productAttributeDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductAttribute(@PathVariable Long id) {
        productAttributeService.deleteProductAttribute(id);
        return ResponseEntity.ok().build();
    }

    // get product attributes by product Id
    @GetMapping("/{productId}/attributes")
    public ResponseEntity<List<ProductAttribute>> getProductAttributesByProductId(@PathVariable Long productId) {
        List<ProductAttribute> productAttributes = productAttributeService.getProductAttributesByProductId(productId);
        return ResponseEntity.ok(productAttributes);
    }

}
