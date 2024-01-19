package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.Product;
import com.UmiUni.shop.model.ProductWithAttributes;
import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.service.SupplierProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers/products")
public class SupplierProductController {

    @Autowired
    private SupplierProductService supplierProductService;

    @Autowired
    private ControllerUtli controllerUtli;

    // Create a product for a specific supplier
    @PostMapping("/{supplierId}/product")
    public ResponseEntity<Product> createProduct(@PathVariable Long supplierId, @RequestBody Product product) {
        return ResponseEntity.ok(supplierProductService.createProduct(supplierId, product));
    }
    @PostMapping("/product")
    public ResponseEntity<Product> createProductByToken(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Product product) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return ResponseEntity.ok(supplierProductService.createProduct(supplierId, product));
    }

    // Get a specific product by ID for a given supplier
    @GetMapping("/{supplierId}/product/{id}")
    public ResponseEntity<Product> getProductByIdAndSupplier(@PathVariable Long supplierId, @PathVariable Long id) {
        return ResponseEntity.ok(supplierProductService.getProductByIdAndSupplier(id, supplierId));
    }
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductByIdAndSupplierToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long id) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return ResponseEntity.ok(supplierProductService.getProductByIdAndSupplier(id, supplierId));
    }

    // Get all products for a specific supplier
    @GetMapping("/{supplierId}/products/all")
    public ResponseEntity<List<Product>> getAllProductsBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierProductService.getAllProductsBySupplier(supplierId));
    }
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProductsBySupplierByToken(@RequestHeader("Authorization") String authorizationHeader) {
        // extract the token from headers
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return ResponseEntity.ok(supplierProductService.getAllProductsBySupplier(supplierId));
    }

    // Update a specific product for a given supplier
    @PutMapping("/{supplierId}/product/{id}")
    public ResponseEntity<Product> updateProductForSupplier(@PathVariable Long supplierId, @PathVariable Long id, @RequestBody Product productDetails) {
        return ResponseEntity.ok(supplierProductService.updateProductForSupplier(id, supplierId, productDetails));
    }
    @PutMapping("/product/{id}")
    public ResponseEntity<Product> updateProductForSupplierByToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long id, @RequestBody Product productDetails) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return ResponseEntity.ok(supplierProductService.updateProductForSupplier(id, supplierId, productDetails));
    }

    // Delete a specific product for a given supplier
    @DeleteMapping("/{supplierId}/product/{id}")
    public ResponseEntity<Void> deleteProductForSupplier(@PathVariable Long supplierId, @PathVariable Long id) {
        supplierProductService.deleteProductForSupplier(id, supplierId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProductForSupplierByToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long id) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        supplierProductService.deleteProductForSupplier(id, supplierId);
        return ResponseEntity.ok().build();
    }

    // Get product with attributes for a specific supplier
    @GetMapping("/{supplierId}/product-with-attributes/{productId}")
    public ResponseEntity<ProductWithAttributes> getProductWithAttributesForSupplier(@PathVariable Long supplierId, @PathVariable Long productId) {
        return ResponseEntity.ok(supplierProductService.getProductWithAttributesForSupplier(productId, supplierId));
    }

    // Get product by SKU code for a specific supplier
    @GetMapping("/{supplierId}/product-by-sku/{skuCode}")
    public ResponseEntity<Product> getProductBySkuCodeForSupplier(@PathVariable Long supplierId, @PathVariable String skuCode) {
        return ResponseEntity.ok(supplierProductService.getProductBySkuCodeForSupplier(skuCode, supplierId));
    }

}
