package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.Customer;
import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.service.CustomerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers/auth/info")
@Log4j2
public class CustomerAuthController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // http://localhost:9001/api/v1/customers/auth/info
    @GetMapping
    public ResponseEntity<Customer> getCustomerByToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            Long customerId = jwtTokenProvider.getIdByRoleFromToken(token);

            Customer customer = customerService.getCustomerById(customerId)
                    .orElseThrow(() -> new RuntimeException("No customer exit!"));
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping
    public ResponseEntity<Customer> updateCustomer(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Customer customerDetails) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            Long customerId = jwtTokenProvider.getIdByRoleFromToken(token);

            Customer updatedCustomer = customerService.updateCustomer(customerId, customerDetails);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCustomer(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            Long customerId = jwtTokenProvider.getIdByRoleFromToken(token);

            customerService.deleteCustomer(customerId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid or missing Authorization header");
    }

}
