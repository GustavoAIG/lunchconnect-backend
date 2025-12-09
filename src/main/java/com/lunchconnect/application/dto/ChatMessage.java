package com.lunchconnect.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    // 💡 ID del grupo al que pertenece el mensaje (tópico de destino)
    private String grupoId;

    // 💡 ID del usuario que envía el mensaje (establecido por el servidor/JWT)
    private String senderId;

    // El contenido del mensaje
    private String content;

    // Timestamp de envío (establecido por el servidor)
    private LocalDateTime timestamp;

    // Opcional: Tipo de mensaje (CHAT, JOIN, LEAVE, SYSTEM)
    private MessageType type;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        SYSTEM
    }
}