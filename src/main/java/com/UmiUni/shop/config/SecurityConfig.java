package com.UmiUni.shop.config;

import com.UmiUni.shop.component.CustomAuthenticationSuccessHandler;
import com.UmiUni.shop.security.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsUtils;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .cors().and() // Ensure CORS is applied before Spring Security
                .authorizeRequests()
                .anyRequest().permitAll()
//                    .antMatchers("/", "/api/v1/suppliers/v2/**", "/api/v1/suppliers/**", "/api/v1/suppliers/products/**").permitAll()
//                    .antMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
//                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
//
//                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .defaultSuccessUrl("/login/success", true)  // Redirect after successful login
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(customAuthenticationSuccessHandler);
    }

}
