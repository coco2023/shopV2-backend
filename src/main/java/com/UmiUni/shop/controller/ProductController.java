package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.model.ProductWithAttributes;
import com.UmiUni.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
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
