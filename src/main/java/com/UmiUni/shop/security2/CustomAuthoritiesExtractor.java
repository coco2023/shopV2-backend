//package com.UmiUni.shop.security;
//
//import com.fasterxml.jackson.databind.JavaType;
//import com.fasterxml.jackson.databind.type.TypeFactory;
//import com.fasterxml.jackson.databind.util.Converter;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Set;
//
//public class CustomAuthoritiesExtractor implements Converter<Jwt, Collection<GrantedAuthority>> {
//    @Override
//    public Collection<GrantedAuthority> convert(Jwt jwt) {
//        Set<GrantedAuthority> authorities = new HashSet<>();
//
//        // Extract the role claim from the token
//        String role = jwt.getClaimAsString("role");
//        if (role != null) {
//            // Prepend "ROLE_" if necessary
//            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//        }
//
//        // If there are other claims that map to authorities, extract them here
//
//        return authorities;
//    }
//
//    @Override
//    public JavaType getInputType(TypeFactory typeFactory) {
//        return null;
//    }
//
//    @Override
//    public JavaType getOutputType(TypeFactory typeFactory) {
//        return null;
//    }
//}
