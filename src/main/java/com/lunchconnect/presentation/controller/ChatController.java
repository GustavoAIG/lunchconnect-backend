package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.ChatMessage;
import com.lunchconnect.domain.model.Grupo;
import com.lunchconnect.domain.model.Mensaje;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.GrupoRepository;
import com.lunchconnect.domain.repository.MensajeRepository;
import com.lunchconnect.domain.repository.UsuarioRepository;
import com.lunchconnect.infrastructure.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional; // Importante para la persistencia

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    // SimpMessagingTemplate: Permite enviar mensajes a tópicos de WebSocket.
    private final SimpMessagingTemplate messagingTemplate;

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;

    /**
     * Maneja los mensajes entrantes de los clientes.
     * * El cliente envía a: /app/chat.sendMessage
     * Spring lo reenvía al broker a: /topic/grupos/{grupoId}
     * * @param chatMessage Mensaje enviado por el cliente.
     * @param userDetails Información del usuario autenticado vía JWT.
     */
    @MessageMapping("/chat.sendMessage")
    @Transactional // Asegura que la persistencia y la notificación se manejen bien
    public void sendMessage(
            ChatMessage chatMessage,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return;

        // 1. Obtener Entidades (Usuario y Grupo)
        // El subject del JWT es el nombre/correo, que usamos para buscar el Usuario
        Usuario remitente = usuarioRepository.findByCorreoElectronico(userDetails.getUsername())
                .or(() -> usuarioRepository.findByNombreUsuario(userDetails.getUsername()))
                .orElseThrow(() -> new NotFoundException("Remitente no encontrado en DB"));

        // El grupoId del DTO se asume que es el ID Long del grupo.
        Long grupoIdLong = Long.valueOf(chatMessage.getGrupoId());
        Grupo grupo = grupoRepository.findById(grupoIdLong)
                .orElseThrow(() -> new NotFoundException("Grupo de chat no encontrado"));

        // 2. Persistir el Mensaje en la DB
        Mensaje mensaje = Mensaje.builder()
                .grupo(grupo)
                .remitente(remitente)
                .contenido(chatMessage.getContent())
                .tipo(Mensaje.TipoMensaje.CHAT)
                .build();

        mensajeRepository.save(mensaje);

        // 3. Rellenar y enviar el DTO (ChatMessage) a través de WebSocket
        chatMessage.setSenderId(remitente.getId().toString()); // Usamos el ID Long para el frontend
        chatMessage.setTimestamp(mensaje.getFechaEnvio()); // Usamos el timestamp de la DB
        chatMessage.setType(ChatMessage.MessageType.CHAT);

        String destination = "/topic/grupos/" + chatMessage.getGrupoId();
        messagingTemplate.convertAndSend(destination, chatMessage);

        log.info("Mensaje persistido y enviado a tópico {} por usuario {}", chatMessage.getGrupoId(), remitente.getId());
    }
}