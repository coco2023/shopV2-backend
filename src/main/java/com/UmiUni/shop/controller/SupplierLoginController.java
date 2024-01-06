//package com.UmiUni.shop.controller;
//
//import com.UmiUni.shop.service.SupplierService;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@RestController
//@RequestMapping("/api/v1/suppliers")
//@Log4j2
//public class SupplierLoginController {
//
//    @Autowired
//    private SupplierService supplierService;
//
//    @GetMapping("/login/success")
//    public ResponseEntity<?> loginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User, HttpServletResponse response) throws IOException {
//        String role = oAuth2User.getAttribute("role"); // Example attribute
//        if ("supplier".equals(role)) {
//            response.sendRedirect("http://localhost:3000");
//            return ResponseEntity.ok("Redirecting to supplier dashboard...");
//        } else {
//            response.sendRedirect("http://localhost:3000");
//            return ResponseEntity.ok("Redirecting to customer dashboard...");
//        }
//    }
//
//}