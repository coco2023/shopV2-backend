package com.UmiUni.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
//                .antMatchers("/", "/api/v1/suppliers/v2/callback", "/api/v1/suppliers/v2/authorize").permitAll()
//                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .defaultSuccessUrl("/api/v1/suppliers/v2/callback", true)
                .and()
                .logout()
                .logoutSuccessUrl("/")
                .permitAll();
    }
}
