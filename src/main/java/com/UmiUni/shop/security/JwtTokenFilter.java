package com.UmiUni.shop.security;

import com.UmiUni.shop.security.exception.CustomException;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
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

        String token = resolveToken(request);
        try {
            log.info("***FilterToken: " + token);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);

                // Extract supplierId from the token
                Long tokenSupplierId = jwtTokenProvider.getSupplierIdFromToken(token);

                // Get supplierId from the request path (if present)
                log.info("request {}, path: {}, getRequestURI: {} ", request, null, request.getRequestURI());
                Long pathSupplierId = extractSupplierIdFromPath(request.getRequestURI());
                log.info("tokenSupplierId: " + tokenSupplierId + "; pathSupplierId: " + pathSupplierId);

                // Check if the supplierId in the token matches the supplierId in the path
                if (pathSupplierId != null && !pathSupplierId.equals(tokenSupplierId)) {
                    throw new AccessDeniedException("Access Denied: You do not have permission to access this resource.");
                }

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("***auth: " + auth);
            }
        } catch (CustomException ex) {
            // this is very important, since it guarantees the user is not authenticated at all
            SecurityContextHolder.clearContext();
            response.sendError(ex.getHttpStatus().value(), ex.getMessage());
            log.info("***FAIL!!!***");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        log.info("request: " + request);
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            log.info("@request, {}, {}", request, bearerToken.substring(7));
            return bearerToken.substring(7);
        }
        return null;
    }

    // Method to extract supplierId from the request path
    public Long extractSupplierIdFromPath(String path) {
        // Implement logic to extract supplierId from the path
        // This is just an example and needs to be implemented based on your API structure
        log.info("***path: " + path);
        Matcher matcher = Pattern.compile("/api/v1/suppliers/(\\d+)").matcher(path);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(1));
        }
        log.info("path: {}, {}" ,path ,matcher.group(1));
        return null;
    }

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
