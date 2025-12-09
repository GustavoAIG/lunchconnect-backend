package com.lunchconnect.application.mapper;

import com.lunchconnect.application.dto.ChatMessage;
import com.lunchconnect.domain.model.Mensaje;
import org.springframework.stereotype.Component;

// Si usas MapStruct, la implementación sería diferente, pero esta es una implementación manual simple:
@Component
public class MensajeMapper {

    /**
     * Convierte una entidad Mensaje a un DTO ChatMessage.
     * Esto se usa principalmente para enviar el historial al cliente (REST).
     */
    public ChatMessage toChatMessage(Mensaje mensaje) {
        if (mensaje == null) {
            return null;
        }

        // 1. Convertir el tipo de mensaje Enum
        ChatMessage.MessageType chatMessageType;
        try {
            // Convertimos el enum de dominio (Mensaje.TipoMensaje) al enum DTO (ChatMessage.MessageType)
            chatMessageType = ChatMessage.MessageType.valueOf(mensaje.getTipo().name());
        } catch (IllegalArgumentException e) {
            // Manejo de error si los enums no coinciden (deberían coincidir)
            chatMessageType = ChatMessage.MessageType.CHAT;
        }

        // 2. Mapear los campos
        return ChatMessage.builder()
                // El ID del grupo (Long) se convierte a String para el DTO del chat
                .grupoId(mensaje.getGrupo().getId().toString())
                // El remitente (Usuario) se convierte a String (su ID) para el DTO
                .senderId(mensaje.getRemitente().getId().toString())
                .content(mensaje.getContenido())
                .timestamp(mensaje.getFechaEnvio())
                .type(chatMessageType)
                .build();
    }
}