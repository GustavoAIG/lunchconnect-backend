package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.MensajePrivadoDTO;
import com.lunchconnect.application.service.ChatPrivadoService;
import com.lunchconnect.domain.model.MensajePrivado;
import com.lunchconnect.infrastructure.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatPrivadoController {

    private final ChatPrivadoService chatPrivadoService;
    private final ChatWebSocketHandler chatWebSocketHandler;

    /**
     * Envía un mensaje privado usando WebSocket plano.
     *
     * @param mensajeDTO DTO del mensaje privado
     * @param remitenteId ID del usuario remitente (obtenido de JWT o interceptor WS)
     */
    public void sendPrivateMessage(MensajePrivadoDTO mensajeDTO, Long remitenteId) {
        // Guardar en BD
        mensajeDTO.setRemitenteId(remitenteId);
        MensajePrivado mensajeGuardado = chatPrivadoService.guardarMensaje(mensajeDTO);

        // Preparar DTO para enviar
        MensajePrivadoDTO respuestaDTO = new MensajePrivadoDTO();
        respuestaDTO.setRemitenteId(mensajeGuardado.getRemitente().getId());
        respuestaDTO.setDestinatarioId(mensajeDTO.getDestinatarioId());
        respuestaDTO.setContenido(mensajeGuardado.getContenido());
        respuestaDTO.setConversacionId(mensajeGuardado.getConversacion().getIdConversacion());
        respuestaDTO.setFechaEnvio(mensajeGuardado.getFechaEnvio());

        // Enviar mensaje a través del WebSocket handler
        chatWebSocketHandler.handleTextMessageDirectly(respuestaDTO);
    }
}
