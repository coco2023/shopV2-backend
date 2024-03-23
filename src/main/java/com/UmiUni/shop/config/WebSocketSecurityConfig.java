package com.UmiUni.shop.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
@Log4j2
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpDestMatchers("/user/queue/**").authenticated()  // Require authentication for subscribing to this topic
                .anyMessage().authenticated();  // Require authentication for any other STOMP message
        log.info("message: {}", messages);
    }

    // Override to disable CSRF within WebSocket if necessary
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
