package com.UmiUni.shop.security.controller;

import com.UmiUni.shop.security.JwtTokenProvider;
import com.UmiUni.shop.security.service.UserService;
import com.UmiUni.shop.security.dto.RegistrationRequestDTO;
import com.UmiUni.shop.security.dto.RegistrationResponseDTO;
import com.UmiUni.shop.security.model.AuthRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

            // Generate a JWT token with roles and permissions included
            String token = jwtTokenProvider.createToken(authentication);
            log.info("***token: " + token);

            // Return the token and user information as needed
            Map<String, Object> model = new HashMap<>();
            model.put("username", data.getUsername());
            model.put("token", token);
            log.info("***model: " + model);

            return ResponseEntity.ok(model);

        } catch (AuthenticationException e) {
            log.error("Authentication error", e); // Log the exception
            return new ResponseEntity<>("Invalid username/password supplied", HttpStatus.UNAUTHORIZED);
        }
    }
}