package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.ChatMessage;
import com.lunchconnect.application.service.ChatService;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.UsuarioRepository;
import com.lunchconnect.infrastructure.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Endpoint para enviar mensajes.
     * El frontend envía a /api/chat/send
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(
            @RequestBody ChatMessage chatMessage,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        // 1. Obtener remitente
        Usuario remitente = usuarioRepository.findByCorreoElectronico(userDetails.getUsername())
                .or(() -> usuarioRepository.findByNombreUsuario(userDetails.getUsername()))
                .orElseThrow(() -> new NotFoundException("Remitente no encontrado"));

        chatMessage.setSenderId(remitente.getId().toString());

        // 2. Delegar al servicio de chat (que se encargará de persistir y enviar por WS)
        chatService.sendMessage(chatMessage);

        log.info("Mensaje enviado por usuario {} al grupo {}", remitente.getId(), chatMessage.getGrupoId());
        return ResponseEntity.ok().build();
    }
}
