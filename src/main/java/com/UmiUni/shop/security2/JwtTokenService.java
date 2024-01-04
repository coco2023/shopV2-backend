//package com.UmiUni.shop.security;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class JwtTokenService {
//
//    private final SecretKey jwtSecret;
//
//    public JwtTokenService() {
//        this.jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Generate secure key
//    }
//
//    @Value("${jwt.expiration.ms}")
//    private int jwtExpirationInMs;
//
//    public String createTokenForSupplier(Long supplierId, String role) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
//
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("role", role);
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(Long.toString(supplierId))
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(jwtSecret)
//                .compact();
//    }
//}
