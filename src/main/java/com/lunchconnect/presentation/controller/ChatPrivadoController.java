package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.MensajePrivadoDTO;
import com.lunchconnect.application.service.ChatPrivadoService;
import com.lunchconnect.domain.model.MensajePrivado;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

// Usamos @Controller para WebSockets, NO @RestController
@Controller
@RequiredArgsConstructor
public class ChatPrivadoController {

    private final ChatPrivadoService chatPrivadoService;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 1. Recibe el mensaje desde el cliente: /app/chat.sendPrivate
     * 2. Guarda el mensaje en la BD.
     * 3. Reenvía el mensaje al destinatario (y al remitente para confirmación)
     */
    @MessageMapping("/chat.sendPrivate")
    public void sendPrivateMessage(MensajePrivadoDTO mensajeDTO, StompHeaderAccessor headerAccessor) {

        // 1. Obtener el ID del remitente autenticado.
        // REQUIERE: Que tu JwtTokenProvider o WebSockAuthInterceptor configure el Principal.
        // Asumimos que el getName() del Principal es el ID del usuario (String).
        String principalName = headerAccessor.getUser().getName();
        Long remitenteId = Long.parseLong(principalName);

        // 2. Llenar DTO y Guardar en BD
        mensajeDTO.setRemitenteId(remitenteId);
        MensajePrivado mensajeGuardado = chatPrivadoService.guardarMensaje(mensajeDTO);

        // 3. Crear DTO de respuesta para el frontend (incluye el ID de la conversación)
        MensajePrivadoDTO respuestaDTO = new MensajePrivadoDTO();
        respuestaDTO.setRemitenteId(mensajeGuardado.getRemitente().getId()); // Usamos getId() de Usuario
        respuestaDTO.setDestinatarioId(mensajeDTO.getDestinatarioId());
        respuestaDTO.setContenido(mensajeGuardado.getContenido());
        respuestaDTO.setConversacionId(mensajeGuardado.getConversacion().getIdConversacion());
        respuestaDTO.setFechaEnvio(mensajeGuardado.getFechaEnvio());

        // 4. Reenviar al Destinatario: /user/{destinatarioId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                mensajeDTO.getDestinatarioId().toString(),
                "/queue/messages",
                respuestaDTO
        );

        // 5. Reenviar al Remitente (para que vea el mensaje en su propia interfaz en tiempo real)
        messagingTemplate.convertAndSendToUser(
                remitenteId.toString(),
                "/queue/messages",
                respuestaDTO
        );
    }
}