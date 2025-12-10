package com.lunchconnect.infrastructure.config;

// 💡 IMPORTACIONES CORREGIDAS AL PAQUETE infrastructure.security
import com.lunchconnect.infrastructure.security.WebSocketAuthInterceptor;
import com.lunchconnect.infrastructure.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
// ... (Otras importaciones) ...
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Inyección con la clase correcta
    private final JwtTokenProvider tokenProvider;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                // Creación del interceptor con el tokenProvider inyectado
                .setInterceptors(new WebSocketAuthInterceptor(tokenProvider));
    }
}