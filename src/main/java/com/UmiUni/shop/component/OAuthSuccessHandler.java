//package com.UmiUni.shop.component;
//
//import com.UmiUni.shop.security.JwtTokenProvider;
//import com.UmiUni.shop.service.SupplierService;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//@Log4j2
//public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        getRedirectStrategy().sendRedirect(request, response, "/api/auth/login/success");
//    }
//}
//
////        // Extract supplier ID or username
////        String username = authentication.getName();
////
////        // get the supplierId
////        Long supplierId = supplierService.getSupplierByName(username).getSupplierId();
////
////        // Generate JWT token
////        String token = jwtTokenProvider.createToken(authentication, supplierId); // Pass null or actual supplierId
////        log.info("token: " + token);
////
////        // Set token in response
////        response.addHeader("Authorization", "Bearer " + token);
//
