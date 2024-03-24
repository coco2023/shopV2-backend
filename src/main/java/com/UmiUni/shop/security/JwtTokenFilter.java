package com.UmiUni.shop.security;

import com.UmiUni.shop.constant.SecurityConstants;
import com.UmiUni.shop.security.exception.CustomException;
import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Log4j2
public class JwtTokenFilter extends OncePerRequestFilter {

    private JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // this method works for saving the token in the header
        String token = resolveToken(request);
        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (CustomException ex) {
            // this is very important, since it guarantees the user is not authenticated at all
            SecurityContextHolder.clearContext();
            response.sendError(ex.getHttpStatus().value(), ex.getMessage());
            return;
        }

        chain.doFilter(request, response);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

//    // Method to extract supplierId from the request path
//    public Long extractSupplierIdFromPath(String path) {
//        // Implement logic to extract supplierId from the path
//        // This is just an example and needs to be implemented based on your API structure
//        log.info("***path: " + path);
//        Matcher matcher = Pattern.compile("/api/v1/suppliers/(\\d+)").matcher(path);
//        if (matcher.find()) {
//            return Long.valueOf(matcher.group(1));
//        }
//        log.info("path: {}, {}" ,path ,matcher.group(1));
//        return null;
//    }
//
//    private String extractJwtTokenFromCookies(HttpServletRequest request) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if ("JWT_TOKEN".equals(cookie.getName())) {
//                    log.info("token in cookies: {}", cookie.getName());
//                    return cookie.getValue();
//                }
//            }
//        }
//        return null;
//    }
//
//    private String extractJwtFromRequest(HttpServletRequest request) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if ("token".equals(cookie.getName())) {
//                    // URL Decode
//                    String decodedToken = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
//                    log.info("success get the cookies! {} , {} ", cookie.getValue(), decodedToken);
//                    return decodedToken; //cookie.getValue();
//                }
//            }
//        }
//        return null;
//    }

}

//        // Bypass filter for OAuth2 login URLs
//        log.info("request: " + request.getPathInfo() + "response: " + response + " chain: " + chain);
//        if (request.getRequestURI().startsWith("/login") || request.getRequestURI().startsWith("/login/oauth2/")) {
////        if (request.getRequestURI().startsWith("www.sandbox.paypal.com") || request.getRequestURI().startsWith("www.sandbox.paypal.com/connect")) {
//            chain.doFilter(request, response);
//            return;
//        }
//
//        String path = request.getServletPath();
//        log.info("path: " + path);


//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
////        String token = extractJwtFromRequest(request);
////        log.info("this is the filter token: " + token);
////        String token = extractJwtTokenFromCookies(request);
//
//        // this method works for saving the token in the header
//        String token = resolveToken(request);
//        try {
//            log.info("111***FilterToken: " + token);
//            if (token != null && jwtTokenProvider.validateToken(token)) {
//                Authentication auth = jwtTokenProvider.getAuthentication(token);
//
//                // Extract supplierId from the token
//                Long tokenSupplierId = jwtTokenProvider.getIdByRoleFromToken(token);
//                log.info("tokenSupplierId, {}", tokenSupplierId);
//
//                // Get supplierId from the request path (if present)
//                log.info("request {}, path: {}, getRequestURI: {} ", request, null, request.getRequestURI());
//                Long pathSupplierId = extractSupplierIdFromPath(request.getRequestURI());
//                log.info("tokenSupplierId: " + tokenSupplierId + "; pathSupplierId: " + pathSupplierId);
//
//                // Check if the supplierId in the token matches the supplierId in the path
//                if (pathSupplierId != null && !pathSupplierId.equals(tokenSupplierId)) {
//                    throw new AccessDeniedException("Access Denied: You do not have permission to access this resource.");
//                }
