package com.UmiUni.shop.controller;

import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ControllerUtli {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public Long getSupplierIdByToken(String authorizationHeader) {
        String token = extractTokenFromHeader(authorizationHeader);
        return jwtTokenProvider.getIdByRoleFromToken(token);
    }

    public String getRoleFromToken(String token) {
        return jwtTokenProvider.getRoleFromToken(token);
    }

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Remove "Bearer " prefix to get the token
        }
        throw new IllegalArgumentException("Invalid or missing Authorization header");
    }

}
