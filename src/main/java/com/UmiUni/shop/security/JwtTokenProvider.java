package com.UmiUni.shop.security;

import com.UmiUni.shop.constant.SecurityConstants;
import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JwtTokenProvider is only needed for creating tokens and not for loading user details
 */
@Component
@Log4j2
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey;

    @Value("${security.jwt.token.expire-length:3600000}") // 1h by default
    private long validityInMilliseconds;

    // Initialization block that encodes the secretKey
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        String username = claims.getSubject();
        log.info("***claims: " + claims);

//        String roles = claims.get("roles").toString();
        String rolesStr = claims.get("roles", String.class);
        List<String> roles = Arrays.asList(rolesStr.split(",")); // Assuming roles are comma-separated
//        List<String> permissions = claims.get("permissions", List.class);

        log.info("***roles: " + roles);
//        log.info("***permissions: " + permissions);

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }
//        if (permissions != null) {
//            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
//        }

        User principal = new User(username, "", authorities);
        UsernamePasswordAuthenticationToken res = new UsernamePasswordAuthenticationToken(principal, token, authorities);
        log.info("***UsernamePasswordAuthenticationToken: " + res);
        return res;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }
            log.info("***Valid Token!!!***");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Here should implement logging and possibly throw an exception depending on the application's needs
            throw new RuntimeException("Expired or invalid JWT token");   //JwtAuthenticationException
        }
    }

    public String createToken(Authentication authentication, Long supplierId) {

        String username = null; // null
        // Check if the authentication is OAuth2
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            // Adjust your claims as needed based on the OAuth2 user info
            username = oauthToken.getPrincipal().getAttribute("email"); // or other identifier
//            claims.put("supplierId", supplierId); // Adjust if you have the supplier ID
        } else {
            username = authentication.getName();
        }
        username = authentication.getName();
        log.info("username: " + username);
        // Collecting roles and permissions from authentication authorities
        List<String> roles = authentication.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        log.info("***roles: " + roles);
//        List<String> permissions = authentication.getAuthorities().stream()
//                .filter(auth -> !auth.getAuthority().startsWith("ROLE_"))
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
//        log.info("***permissions: " + permissions);

        Claims claims = Jwts.claims().setSubject(username);
//        claims.put("roles", "SUPPLIER"); // role
        if (username != null) {
            if (username.endsWith("@business.example.com")) {
                claims.put("roles", "SUPPLIER"); // role
                claims.put("supplierId", supplierId); // Add supplierId to the claims
            } else {
                claims.put("roles", "SUPPLIER"); // role
                claims.put("supplierId", supplierId); // Add supplierId to the claims
            }
        } else {
            throw new IllegalArgumentException("Unable to determine user type: Email is missing");
        }
//        claims.put("supplierId", supplierId); // Add supplierId to the claims
//        claims.put("permissions", permissions);
        log.info("***Claims: " + claims);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return SecurityConstants.TOKEN_PREFIX + token;
    }

    // Method to extract supplierId from the token
    public Long getSupplierIdFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        return claims.get("supplierId", Long.class);
    }
}