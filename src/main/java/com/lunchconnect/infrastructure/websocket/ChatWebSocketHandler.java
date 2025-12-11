package com.lunchconnect.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunchconnect.application.dto.ChatMessage;
import com.lunchconnect.application.dto.MensajePrivadoDTO;
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

    // Map de grupoId -> sesiones activas (para chats de grupo)
    private final Map<Long, Set<WebSocketSession>> sessionsByGroup = new ConcurrentHashMap<>();

    // Map de userId -> sesiones activas (para chats privados)
    private final Map<Long, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Nueva conexión WebSocket: {}", session.getId());

        // Registrar sesión de usuario para chats privados si existe userId en headers
        String userIdHeader = session.getAttributes().get("userId") != null ?
                session.getAttributes().get("userId").toString() : null;

        if (userIdHeader != null) {
            Long userId = Long.parseLong(userIdHeader);
            sessionsByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        }
    }

    // Enviar mensaje a todos los usuarios de un grupo
    public void handleTextMessageDirectly(ChatMessage chatMessage) {
        try {
            Long grupoId = Long.valueOf(chatMessage.getGrupoId());
            String payload = objectMapper.writeValueAsString(chatMessage);

            sessionsByGroup.getOrDefault(grupoId, Set.of())
                    .forEach(s -> {
                        try { if (s.isOpen()) s.sendMessage(new TextMessage(payload)); }
                        catch (Exception e) { log.error("Error WS grupo: {}", e.getMessage()); }
                    });
        } catch (Exception e) {
            log.error("Error enviando mensaje directo grupo: {}", e.getMessage());
        }
    }

    // Nuevo método para enviar mensajes privados
    public void handleTextMessageDirectly(MensajePrivadoDTO mensajePrivadoDTO) {
        try {
            String payload = objectMapper.writeValueAsString(mensajePrivadoDTO);

            // Enviar al destinatario
            sessionsByUser.getOrDefault(mensajePrivadoDTO.getDestinatarioId(), Set.of())
                    .forEach(s -> {
                        try { if (s.isOpen()) s.sendMessage(new TextMessage(payload)); }
                        catch (Exception e) { log.error("Error WS privado: {}", e.getMessage()); }
                    });

            // Enviar al remitente
            sessionsByUser.getOrDefault(mensajePrivadoDTO.getRemitenteId(), Set.of())
                    .forEach(s -> {
                        try { if (s.isOpen()) s.sendMessage(new TextMessage(payload)); }
                        catch (Exception e) { log.error("Error WS privado remitente: {}", e.getMessage()); }
                    });
        } catch (Exception e) {
            log.error("Error enviando mensaje privado WS: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionsByGroup.values().forEach(s -> s.remove(session));
        sessionsByUser.values().forEach(s -> s.remove(session));
        log.info("Conexión cerrada WebSocket: {}", session.getId());
    }
}

