package com.UmiUni.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@RestController
public class CorsConfig implements WebMvcConfigurer {

    @Value("${local-auth-backend}")
    private String ngrokUrl;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:9001");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("https://www.sandbox.paypal.com");
        config.addAllowedOrigin("https://api.sandbox.paypal.com");
        config.addAllowedOrigin(ngrokUrl);
        config.addAllowedOrigin("https://www.quickmall24.com"); // Replace with the origin of your React app
        config.addAllowedOrigin("https://v2.quickmall24.com"); // Replace with the origin of your React app
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
         config.addAllowedMethod("DELETE"); // No deletion at any time
        config.addExposedHeader("Access-Control-Allow-Origin");
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
