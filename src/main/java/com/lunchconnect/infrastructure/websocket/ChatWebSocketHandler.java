package com.lunchconnect.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunchconnect.application.dto.ChatMessage;
import com.lunchconnect.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    // <grupoId, lista de sesiones>
    private final Map<Long, Set<WebSocketSession>> groupSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery(); // token y grupoId
        if (query == null || !query.contains("token") || !query.contains("groupId")) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String token = Arrays.stream(query.split("&"))
                .filter(s -> s.startsWith("token="))
                .map(s -> s.substring(6))
                .findFirst()
                .orElse("");

        if (!tokenProvider.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token inválido"));
            return;
        }

        Long groupId = Long.parseLong(Arrays.stream(query.split("&"))
                .filter(s -> s.startsWith("groupId="))
                .map(s -> s.substring(8))
                .findFirst()
                .orElse("0"));

        groupSessions.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).add(session);

        log.info("Nueva sesión WS conectada al grupo {}: {}", groupId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
        Long groupId = Long.valueOf(chatMessage.getGrupoId());

        // Reenviar a todos los miembros del grupo
        Set<WebSocketSession> sessions = groupSessions.getOrDefault(groupId, Collections.emptySet());
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        groupSessions.values().forEach(sessions -> sessions.remove(session));
        log.info("Sesión WS cerrada: {}", session.getId());
    }

    public void broadcastToGroup(Long groupId, ChatMessage chatMessage) {
        Set<WebSocketSession> sessions = groupSessions.getOrDefault(groupId, Collections.emptySet());
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                }
            } catch (Exception e) {
                log.error("Error al enviar mensaje WS: {}", e.getMessage());
            }
        });
    }

}
