package com.lunchconnect.infrastructure.websocket;

import com.lunchconnect.infrastructure.security.JwtTokenProvider;
import com.lunchconnect.infrastructure.security.CustomUserDetailsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    // Map para manejar sesiones por usuario
    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromUri(session);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token inválido"));
            return;
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        sessions.put(userId, session);
        System.out.println("WebSocket conectado: " + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Aquí parseas el JSON del mensaje y envías a los destinatarios
        String payload = message.getPayload();
        System.out.println("Mensaje recibido: " + payload);

        // Ejemplo simple: reenviar a todos los usuarios
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) s.sendMessage(new TextMessage(payload));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().remove(session);
        System.out.println("WebSocket desconectado");
    }

    private String getTokenFromUri(WebSocketSession session) {
        // Esperamos token en query param: ws://.../ws?token=xxx
        String query = session.getUri().getQuery(); // token=xxx
        if (query != null && query.startsWith("token=")) {
            return query.substring(6);
        }
        return null;
    }
}
