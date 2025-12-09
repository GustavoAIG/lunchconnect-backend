package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.ChatMessage;
import com.lunchconnect.application.service.GrupoService; // Usamos este servicio
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final GrupoService grupoService;

    /**
     * Endpoint REST para obtener el historial de mensajes de un grupo.
     * Se accede típicamente al cargar la vista de chat.
     */
    @GetMapping("/historial/{grupoId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long grupoId) {
        // Nota: Deberías añadir una capa de seguridad aquí para verificar que el
        // usuario autenticado realmente pertenece al grupo antes de devolver el historial.

        List<ChatMessage> historial = grupoService.obtenerHistorialChat(grupoId);
        return ResponseEntity.ok(historial);
    }
}