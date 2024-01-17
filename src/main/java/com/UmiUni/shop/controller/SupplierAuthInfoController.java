package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.service.SupplierService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/suppliers/auth/info")
@Log4j2
public class SupplierAuthInfoController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<Supplier> getSupplierByToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            Long supplierId = jwtTokenProvider.getSupplierIdFromToken(token);

            Supplier supplier = supplierService.getSupplier(supplierId);
            return ResponseEntity.ok(supplier);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Remove "Bearer " prefix to get the token
        }
        throw new IllegalArgumentException("Invalid or missing Authorization header");
    }

}
