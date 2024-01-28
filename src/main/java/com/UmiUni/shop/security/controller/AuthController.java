package com.UmiUni.shop.security.controller;

import com.UmiUni.shop.constant.UserType;
import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.security.JwtTokenFilter;
import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.security.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * PayPal Oauth
 */
@RestController
@RequestMapping("/auth")
@Log4j2
public class AuthController {

    @Value("${paypal.login-success-redirect.uri}")
    private String loginSuccessFrontendHost;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Welcome to the Home Page!");
    }

    @GetMapping("/home")
    public ResponseEntity<String> homePage() {
        return ResponseEntity.ok("This is the Home Page.");
    }

    @GetMapping("/login/success")
    public void loginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User, OAuth2AuthenticationToken authentication, HttpServletResponse response) throws IOException {

        log.info("* oAuth2User : {}, authentication: {} ", oAuth2User, authentication);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name"); // Adjust according to the data provided by PayPal

        Supplier supplier = userService.registerOrUpdateSupplier(oAuth2User); // Implement this method as per your requirement
        Long supplierId = supplier.getId();
//        String role = oAuth2User.getAttribute("role");

        // Create JWT token
        String token = jwtTokenProvider.createToken(authentication, supplierId, null); // userType here is for password login
        String role = jwtTokenProvider.getRoleFromToken(token);
        Authentication authRes = jwtTokenProvider.getAuthentication(token);
        log.info("role: {}, token: {}, auth: {}", role, token, authRes);
//        Cookie cookie = new Cookie("authToken", token);
//        log.info("cookie: {}", cookie);
//        response.addCookie(cookie);

        if (UserType.SUPPLIER.name().equals(role)) {
            // Redirect to supplier dashboard
//             response.sendRedirect("http://localhost:3000/supplier/profile/" + supplierId + "?token=" + token);
//            response.sendRedirect("http://localhost:3000/supplier/profile/" + supplierId);
            response.sendRedirect(loginSuccessFrontendHost + "/supplier/profile" + "?token=" + token); // middlepage profile
        } else if (UserType.CUSTOMER.name().equals(role)){
//            response.sendRedirect("http://localhost:3000/supplier/profile/" + supplierId + "?token=" + token);
//            response.sendRedirect("http://localhost:3000/supplier/profile/" + supplierId);
            response.sendRedirect(loginSuccessFrontendHost + "/customer/main"  + "?token=" + token);
        }
    }

    @GetMapping("/login/failure")
    public String loginFailure() {
        return "Login failed. Please try again.";
    }

//    @GetMapping("/login/success/v1")
//    public ResponseEntity<?> loginSuccess1(@AuthenticationPrincipal OAuth2User oAuth2User) {
//        // Determine if the user is a supplier or customer
//        String userType = determineUserType(oAuth2User.getAttributes());
//
//        if ("supplier".equals(userType)) {
//            userService.registerOrUpdateSupplier(oAuth2User);
//            return ResponseEntity.ok("Login successful - Redirecting to supplier dashboard..." + oAuth2User.getAttribute("name"));
//        } else {
//            userService.registerOrUpdateCustomer(oAuth2User);
//            return ResponseEntity.ok("Login successful - Redirecting to customer dashboard..." + oAuth2User.getAttribute("name"));
//        }
//    }
//
//    private String determineUserType(Map<String, Object> attributes) {
//        // Implement your logic to determine user type
//        return "supplier"; // or "supplier"
//    }
}
