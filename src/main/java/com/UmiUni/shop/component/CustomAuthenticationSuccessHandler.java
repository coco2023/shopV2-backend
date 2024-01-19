package com.UmiUni.shop.component;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Redirect to the /auth/login/success endpoint
        getRedirectStrategy().sendRedirect(request, response, "/auth/login/success");
    }
}

//        // Here, you can use the authentication object to check user's roles and decide redirection
//        // Example: if user has role ROLE_USER, redirect to /user/dashboard
//
//        if (authentication.getAuthorities().toString().contains("ROLE_USER")) {
//            getRedirectStrategy().sendRedirect(request, response, "/user/dashboard");
//        } else {
//            getRedirectStrategy().sendRedirect(request, response, "/default");
//        }
