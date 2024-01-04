//package com.UmiUni.shop.security2;
//
//import com.UmiUni.shop.exception.CustomException;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//@Log4j2
//public class JwtTokenFilter extends OncePerRequestFilter {
//
//    private JwtTokenProvider jwtTokenProvider;
//
//    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
//        this.jwtTokenProvider = jwtTokenProvider;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
//        String path = request.getServletPath();
//
//        String token = resolveToken(request);
//        try {
//            log.info("***FilterToken: " + token);
//            if (token != null && jwtTokenProvider.validateToken(token)) {
//                Authentication auth = jwtTokenProvider.getAuthentication(token);
//                SecurityContextHolder.getContext().setAuthentication(auth);
//                log.info("***auth: " + auth);
//            }
//        } catch (CustomException ex) {
//            // this is very important, since it guarantees the user is not authenticated at all
//            SecurityContextHolder.clearContext();
//            response.sendError(ex.getHttpStatus().value(), ex.getMessage());
//            log.info("***FAIL!!!***");
//            return;
//        }
//
//        chain.doFilter(request, response);
//    }
//
//    private String resolveToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
//}