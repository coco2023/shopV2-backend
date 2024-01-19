package com.UmiUni.shop.security;

import com.UmiUni.shop.constant.SecurityConstants;
import com.UmiUni.shop.constant.UserType;
import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.parameters.P;
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

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }
            log.info("***Valid Token!!!***");
            return true;
        }catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Unsupported JWT token");
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Malformed JWT token");
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT signature");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT token compact of handler are invalid");
        }
        catch (JwtException e) {
            // Here should implement logging and possibly throw an exception depending on the application's needs
            throw new RuntimeException("JwtException: Expired or invalid JWT token");   //JwtAuthenticationException
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        String username = claims.getSubject();
        log.info("***claims: " + claims);

        String rolesStr = claims.get("roles", String.class);
        List<String> roles = Arrays.asList(rolesStr.split(",")); // Assuming roles are comma-separated
//        List<String> permissions = claims.get("permissions", List.class);

        log.info("getAuth***roles: " + roles);
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

    public String createToken(Authentication authentication, Long supplierId, String userType) {

        log.info("this is the createToken auth: " + authentication);
        String username = null; // null
        Claims claims = null;
        // Check if the authentication is OAuth2
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            // Adjust your claims as needed based on the OAuth2 user info
            username = oauthToken.getPrincipal().getAttribute("email"); // or other identifier
            claims = Jwts.claims().setSubject(username);
            adjustUserRolesOauth(username, claims, supplierId);
        } else {
            username = authentication.getName();
            claims = Jwts.claims().setSubject(username);
            adjustUserRolesPassword(username, claims, supplierId, userType);
        }
        log.info("username: " + username);
        log.info("***Claims: " + claims);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

//        return SecurityConstants.TOKEN_PREFIX + token;
        return token;
    }

    private void adjustUserRolesPassword(String username, Claims claims, Long userId, String userType) {
        log.info("userType: {}", userType);
        if (userType.equals(UserType.SUPPLIER.name())) {
            claims.put("roles", "SUPPLIER"); // role
            claims.put("supplierId", userId); // Add supplierId to the claims
        } else if (userType.equals(UserType.CUSTOMER.name())) {
            claims.put("roles", "CUSTOMER"); // role
            claims.put("customerId", userId); // Add customerId to the claims
        } else {
            claims.put("roles", "ADMIN"); // role
            claims.put("userId", userId); // Add customerId to the claims
        }
    }

    private void adjustUserRolesOauth(String username, Claims claims, Long supplierId) {
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
    }

    // Method to extract supplierId from the token
    public Long getIdByRoleFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        String role = getRoleFromToken(token);
        // assuming only one role for the user
        if (role.equals(UserType.SUPPLIER.name())) {
            return claims.get("supplierId", Long.class);
        } else if (role.equals(UserType.CUSTOMER.name())) {
            return claims.get("customerId", Long.class);
        } else {
            return claims.get("userId", Long.class);
        }
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        return claims.get("roles", String.class);
    }

}


//        username = authentication.getName();
//        log.info("username: " + username);
                // Collecting roles and permissions from authentication authorities
//        List<String> roles = authentication.getAuthorities().stream()
//                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
//        log.info("***roles: " + roles);
//        List<String> permissions = authentication.getAuthorities().stream()
//                .filter(auth -> !auth.getAuthority().startsWith("ROLE_"))
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
//        log.info("***permissions: " + permissions);

//        Claims claims = Jwts.claims().setSubject(username);
//        claims.put("roles", "SUPPLIER"); // role
//        claims.put("supplierId", supplierId); // Add supplierId to the claims

//        claims.put("roles", "SUPPLIER"); // role

//        adjustUserRoles(username, claims, supplierId);

//        if (username != null) {
//            if (username.endsWith("@business.example.com")) {
//                claims.put("roles", "SUPPLIER"); // role
//                claims.put("supplierId", supplierId); // Add supplierId to the claims
//            } else {
//                claims.put("roles", "SUPPLIER"); // role
//                claims.put("supplierId", supplierId); // Add supplierId to the claims
//            }
//        } else {
//            throw new IllegalArgumentException("Unable to determine user type: Email is missing");
//        }
//        claims.put("roles", roles);
//        claims.put("supplierId", supplierId); // Add supplierId to the claims
//        claims.put("permissions", permissions);
//                log.info("***Claims: " + claims);
//                Date now = new Date();
//                Date validity = new Date(now.getTime() + validityInMilliseconds);
//
//                String token = Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(validity)
//                .signWith(SignatureAlgorithm.HS256, secretKey)
//                .compact();
