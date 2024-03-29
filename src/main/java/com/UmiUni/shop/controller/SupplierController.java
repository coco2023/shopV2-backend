package com.UmiUni.shop.controller;

import com.UmiUni.shop.dto.PaypalConfigurationDto;
import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.service.SupplierService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@Log4j2
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ControllerUtli controllerUtli;

    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        return ResponseEntity.ok(supplierService.createSupplier(supplier));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplier(id));
    }
    @GetMapping()
    public ResponseEntity<Supplier> getSupplierByIdByToken(@RequestHeader("Authorization") String authorizationHeader) {

        // extract the token from headers
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);

        return ResponseEntity.ok(supplierService.getSupplier(supplierId));
    }

    @GetMapping("/all")
    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplierDetails) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplierDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok().build();
    }


}
