package com.UmiUni.shop.config;

import com.UmiUni.shop.component.CustomAuthenticationFailureHandler;
import com.UmiUni.shop.component.CustomAuthenticationSuccessHandler;
import com.UmiUni.shop.constant.SecurityConstants;
import com.UmiUni.shop.security.*;
import com.UmiUni.shop.security.service.CustomOAuth2UserService;
import com.UmiUni.shop.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

//    @Autowired
//    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
//                .httpBasic().disable()
//                .csrf().disable()
//                .cors().and() // Ensure CORS is applied before Spring Security
////                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
////                .and()
//                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
//                .and()

                .authorizeRequests()
//                .anyRequest().permitAll()
                    .antMatchers("/api/v1/suppliers/v2/**", "/api/v1/salesOrders/**", "/api/v1/salesOrderDetails/**", "/api/v1/payments/**", "/login/**").permitAll()
                .antMatchers(SecurityConstants.SWAGGER_WHITELIST).permitAll()
                .antMatchers(SecurityConstants.H2_CONSOLE).permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/login/oauth2/code/**").permitAll() // Permit OAuth2 callback

                // Require the SUPPLIER role for supplier-specific endpoints
                .antMatchers("/api/v1/suppliers/**").hasRole("SUPPLIER")

                .anyRequest().authenticated()

                .and()
                .oauth2Login()
//                .defaultSuccessUrl("/api/auth/register/success", true)  // Redirect after successful login
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(customAuthenticationFailureHandler)

//                .oauth2Login(oauth2 -> oauth2
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(customOAuth2UserService) // Handle OAuth2 user info  this.oauthUserService
//                        )
//                        .successHandler(oAuthSuccessHandler)
//                        .failureHandler(customAuthenticationFailureHandler)
//                ) // Handle success login  this.oauthSuccessHandler

                .and()
                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder);
    }

    public void configure(WebSecurity web) {
        web.debug(true);
    }

}
//                .and()
//                .oauth2Login()
//                .defaultSuccessUrl("/api/auth/register/success", true)  // Redirect after successful login
//                .userInfoEndpoint()
//                .userService(customOAuth2UserService)
//                .and()
//                .successHandler(customAuthenticationSuccessHandler)
