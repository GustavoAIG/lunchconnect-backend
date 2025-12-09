package com.lunchconnect.infrastructure.config; // O un paquete de configuración similar

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 💡 HABILITA LA CREACIÓN DE SIMP MESSAGING TEMPLATE
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo para suscripciones de clientes (ej: /topic/grupos/123)
        config.enableSimpleBroker("/topic");
        // Prefijo para endpoints a donde los clientes enviarán mensajes (ej: /app/chat)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para la conexión inicial de WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*"); // Permite cualquier origen (CORS)
    }
}