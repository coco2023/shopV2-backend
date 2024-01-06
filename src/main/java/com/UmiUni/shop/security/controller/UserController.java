package com.UmiUni.shop.security.controller;

import com.UmiUni.shop.constant.UserType;
import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.security.service.UserService;
import com.UmiUni.shop.security.dto.RegistrationRequestDTO;
import com.UmiUni.shop.security.dto.RegistrationResponseDTO;
import com.UmiUni.shop.security.model.AuthRequest;
import com.UmiUni.shop.service.SupplierService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Log4j2
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private SupplierService supplierService;

    // http://localhost:9001/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody RegistrationRequestDTO registrationRequestDTO) {

        try {
            RegistrationResponseDTO registeredUser = userService.registerNewUser(registrationRequestDTO);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        }
        catch (Exception e) {
            // Generic exception handler for other unexpected exceptions
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "An error occurred during registration."));
        }
    }

    // http://localhost:9001/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest data) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            data.getUsername(),
                            data.getPassword()
                    )
            );

            log.info("***authentication: " + authentication);
            // If authentication was successful, set the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // get supplierId by username
            Long supplierId = supplierService.getSupplierByName(data.getUsername()).getSupplierId();
            log.info("supplierId, {}", supplierId);

            // Generate a JWT token with roles and permissions included
            String token = jwtTokenProvider.createToken(authentication, supplierId);
            log.info("***token: " + token);

            // Return the token and user information as needed
            Map<String, Object> model = new HashMap<>();
            model.put("supplierId", supplierId);
            model.put("username", data.getUsername());
            model.put("token", token);
            log.info("***model: " + model);

            return ResponseEntity.ok(model);

        } catch (AuthenticationException e) {
            log.error("Authentication error", e); // Log the exception
            return new ResponseEntity<>("Invalid username/password supplied", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/login/success")
    public void handleLoginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User, OAuth2AuthenticationToken authentication, HttpServletResponse response) throws IOException {
//        OAuth2User oAuth2User = authentication.getPrincipal();

        log.info(oAuth2User);

        // Extract information from OAuth2User
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name"); // Adjust according to the data provided by PayPal

        // Handle user registration or update
        Supplier supplier = userService.registerOrUpdateSupplier(oAuth2User); // Implement this method as per your requirement

        // Create JWT token
        String token = jwtTokenProvider.createToken(authentication, supplier.getSupplierId());
        log.info("token, {}", token);

        // Redirect to frontend with the token (adjust the URL as needed)
        response.sendRedirect("http://localhost:3000/supplier/profile?token=" + token);
    }

}