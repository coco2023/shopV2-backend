//package com.UmiUni.shop.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig2 extends WebSecurityConfigurerAdapter {
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//
//        http
//                .cors().and()
//                .csrf().disable() // Disable CSRF protection for stateless session management
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions
//                .and()
//
//                .authorizeRequests()
//                    .antMatchers("/", "/api/v1/suppliers/v2/**", "/api/v1/suppliers/**", "/api/v1/suppliers/products/**").permitAll()
//                    .antMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
////                    .antMatchers("/api/v1/suppliers/**").hasRole("SUPPLIER")
////                    .antMatchers("/api/v1/suppliers/products/**").hasRole("SUPPLIER")
//                .anyRequest().authenticated()
//
//                .and()
//                    .oauth2Login()
////                  .defaultSuccessUrl("/api/v1/suppliers/v2/callback", true)
//
//                .and()
//                    .logout()
//                    .logoutSuccessUrl("/")
//                    .permitAll();
//
////                .and()
////                    .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
//    }
//}
