package com.lunchconnect.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunchconnect.application.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Map de grupoId -> sesiones activas
    private final Map<Long, Set<WebSocketSession>> sessionsByGroup = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Nueva conexión WebSocket: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            Long grupoId = Long.valueOf(chatMessage.getGrupoId());

            // Registrar la sesión en el grupo si aún no está
            sessionsByGroup.computeIfAbsent(grupoId, k -> ConcurrentHashMap.newKeySet()).add(session);

            // Enviar mensaje a todos los miembros del grupo
            String payload = objectMapper.writeValueAsString(chatMessage);
            sessionsByGroup.get(grupoId).forEach(s -> {
                try {
                    if (s.isOpen()) s.sendMessage(new TextMessage(payload));
                } catch (Exception e) {
                    log.error("Error enviando WS al grupo {}: {}", grupoId, e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Error manejando mensaje WebSocket: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Eliminar la sesión de todos los grupos
        sessionsByGroup.values().forEach(s -> s.remove(session));
        log.info("Conexión cerrada WebSocket: {}", session.getId());
    }

    public void handleTextMessageDirectly(ChatMessage chatMessage) {
        try {
            Long grupoId = Long.valueOf(chatMessage.getGrupoId());
            String payload = objectMapper.writeValueAsString(chatMessage);
            sessionsByGroup.getOrDefault(grupoId, Set.of())
                    .forEach(s -> {
                        try {
                            if (s.isOpen()) s.sendMessage(new TextMessage(payload));
                        } catch (Exception e) { log.error("Error WS: {}", e.getMessage()); }
                    });
        } catch (Exception e) {
            log.error("Error enviando mensaje directo: {}", e.getMessage());
        }
    }

}
