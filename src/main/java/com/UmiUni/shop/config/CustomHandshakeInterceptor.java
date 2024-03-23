package com.UmiUni.shop.config;

import com.UmiUni.shop.controller.ControllerUtli;
import com.UmiUni.shop.security.JwtTokenFilter;
import com.UmiUni.shop.security.JwtTokenProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Log4j2
@Component
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public CustomHandshakeInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = extractToken(request);
        log.info("token: {}", token);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            log.info("authentication: {}", authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Optionally set the supplierId as an attribute if needed elsewhere
            attributes.put("username", authentication.getName());   // the principle's key
        }
        return true;
    }

    private String extractToken(ServerHttpRequest request) {
        // Extract the token from query parameters
        String query = request.getURI().getQuery();
        log.info("this is query: " + query);
        if (query.startsWith("token=Bearer ")) {
            return  query.substring(13);
        }
        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        SecurityContextHolder.clearContext(); // Clear the security context

        if (exception != null) {
            // Log the exception if the handshake failed
            log.error("Handshake failed due to an exception: ", exception);
        } else {
            // Log successful handshake
            log.info("Handshake was successful.");
        }
    }
}
