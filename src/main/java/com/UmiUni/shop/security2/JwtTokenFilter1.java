//package com.UmiUni.shop.security2;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jws;
//import io.jsonwebtoken.Jwts;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.GenericFilterBean;
//
//import javax.crypto.SecretKey;
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class JwtTokenFilter1 extends GenericFilterBean {
//
//    private final SecretKey jwtSecret;
//
//    public JwtTokenFilter1(SecretKey jwtSecret) {
//        this.jwtSecret = jwtSecret;
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        String authHeader = httpRequest.getHeader("Authorization");
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String jwtToken = authHeader.substring(7);
//            try {
//                Jws<Claims> claimsJws = Jwts.parserBuilder()
//                        .setSigningKey(jwtSecret)
//                        .build()
//                        .parseClaimsJws(jwtToken);
//
//                String subject = claimsJws.getBody().getSubject();
//                List<String> roles = claimsJws.getBody().get("roles", List.class);
//
//                List<SimpleGrantedAuthority> authorities = roles.stream()
//                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                        .collect(Collectors.toList());
//
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(subject, null, authorities);
//
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            } catch (Exception e) {
//                // Handle the exception by clearing the context
//                SecurityContextHolder.clearContext();
//            }
//        }
//
//        chain.doFilter(request, response);
//    }
//}
