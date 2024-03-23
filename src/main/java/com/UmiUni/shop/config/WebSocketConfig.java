package com.UmiUni.shop.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
@Log4j2
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private CustomHandshakeInterceptor customHandshakeInterceptor;

    @Value("${paypal.frontend.base.uri}")
    private String frontendHostUrl;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendHostUrl) // Use frontend's origin here  ("*") allow all origins
                .withSockJS()
                .setInterceptors(customHandshakeInterceptor); // Use the autowired interceptor
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user"); // Ensure this is set for user-specific destinations
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                log.info("authentication: {}", authentication);
                if (authentication != null && authentication.isAuthenticated()) {
                    Principal principal = new Principal() {
                        @Override
                        public String getName() {
                            return authentication.getName(); // This return the supplierName
                        }
                    };
                    // the principle and authorization store the username for verification during jwtToken creation process
                    log.info("principal: {}", principal.getName());
                    accessor.setUser(principal);
                }
                log.info("StompHeaderAccessor: {}, message: {}, getUser: {}", accessor, message, accessor.getUser().getName());
                return message;
            }
        });
    }
}

//    // 禁用CSRF保护特定于WebSocket
//    @Override
//    protected boolean sameOriginDisabled() {
//        return true;
//    }

//    @Autowired
//    private ControllerUtli controllerUtli;
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws")
//                .setAllowedOrigins("*")
//                .withSockJS()
//                .setInterceptors(new HandshakeInterceptor() {
//                    @Override
//                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//                        // Extract and validate the JWT token from the request
//                        // Set the supplierId as an attribute if the token is valid
//                        attributes.put("supplierId", controllerUtli.getSupplierIdByToken(attributes));
//                        return true;
//                    }
//
//                    @Override
//                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
//                    }
//                });
//    }

