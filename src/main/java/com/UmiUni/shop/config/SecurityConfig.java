package com.UmiUni.shop.config;

import com.UmiUni.shop.component.CustomAuthenticationSuccessHandler;
import com.UmiUni.shop.constant.SecurityUrlConstants;
import com.UmiUni.shop.security.JwtTokenFilter;
import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.security.service.CustomOAuth2UserService;
import com.UmiUni.shop.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebSecurity
@EnableWebMvc
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .cors().and() // Ensure CORS is applied before Spring Security

                .authorizeRequests()
                .antMatchers(SecurityUrlConstants.PUBLIC_URLS).permitAll()
                .antMatchers(SecurityUrlConstants.SWAGGER_WHITELIST).permitAll()
                .antMatchers(SecurityUrlConstants.H2_CONSOLE).permitAll()
                .antMatchers(HttpMethod.GET, SecurityUrlConstants.PUBLIC_READ_URLS).permitAll()
                .antMatchers(HttpMethod.POST, SecurityUrlConstants.PUBLIC_CREATE_URLS).permitAll()

//                .antMatchers("/api/proxy/paypal").permitAll() // Allow unauthenticated access to the proxy

                .antMatchers(SecurityUrlConstants.SUPPLIER_PUBLIC_URLS).hasRole("SUPPLIER")
                .antMatchers(SecurityUrlConstants.CUSTOMER_PUBLIC_URLS).hasRole("CUSTOMER")
                .antMatchers(SecurityUrlConstants.ADMIN_PUBLIC_URLS).hasAnyRole("ADMIN", "TESTER")

                // Permit all requests to WebSocket endpoints
                .antMatchers("/ws/**").permitAll()

//                .anyRequest().permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(customAuthenticationSuccessHandler)
                .and()
                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // jwtTokenFilter
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

}
