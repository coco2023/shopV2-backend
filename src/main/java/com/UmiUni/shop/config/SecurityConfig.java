package com.UmiUni.shop.config;

import com.UmiUni.shop.component.CustomAuthenticationSuccessHandler;
import com.UmiUni.shop.constant.SecurityConstants;
import com.UmiUni.shop.security.JwtTokenFilter;
import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.security.service.CustomOAuth2UserService;
import com.UmiUni.shop.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
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
                .antMatchers("/home", "/login").permitAll()
                .antMatchers("/", "/auth/**").permitAll()
                .antMatchers("/api/v1/suppliers/v2/**", "/api/v1/salesOrders/**", "/api/v1/salesOrderDetails/**", "/api/v1/payments/**", "/login/**").permitAll()
                .antMatchers(SecurityConstants.SWAGGER_WHITELIST).permitAll()
                .antMatchers(SecurityConstants.H2_CONSOLE).permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/login/oauth2/code/**").permitAll() // Permit OAuth2 callback

//                .antMatchers("/api/proxy/paypal").permitAll() // Allow unauthenticated access to the proxy

                // Require the SUPPLIER role for supplier-specific endpoints
                .antMatchers("/api/v1/suppliers/**").hasRole("SUPPLIER")

                .anyRequest().authenticated()
                .and()
                .oauth2Login()
//                .defaultSuccessUrl("/login/success", true)  // Redirect after successful login
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(customAuthenticationSuccessHandler)
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

}

//                .defaultSuccessUrl("/loginSuccess")
//                .failureUrl("/loginFailure");


//        http
//                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/", "/auth/**").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .oauth2Login()
//                .loginPage("/login")
//                .defaultSuccessUrl("/auth/login/success", true)
//                .failureUrl("/auth/login/failure");
