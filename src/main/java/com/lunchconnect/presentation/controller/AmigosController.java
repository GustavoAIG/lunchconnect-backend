package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.service.AmigosService;
import com.lunchconnect.application.service.dto.UsuarioMinDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/amigos") // RUTA BASE PARA TODOS LOS ENDPOINTS DE AMISTAD
public class AmigosController {

    private final AmigosService amigosService;

    // Inyección de dependencia
    public AmigosController(AmigosService amigosService) {
        this.amigosService = amigosService;
    }

    // --- 1. BUSCAR USUARIOS (GET /api/amigos/buscar?term=query) ---
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioMinDTO>> buscarUsuarios(@RequestParam String term) {
        try {
            Long currentUserId = amigosService.getCurrentAuthenticatedUserId();
            List<UsuarioMinDTO> usuarios = amigosService.searchUsers(currentUserId, term);
            return ResponseEntity.ok(usuarios);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- 2. ENVIAR SOLICITUD (POST /api/amigos/solicitar/{destinatarioId}) ---
    @PostMapping("/solicitar/{destinatarioId}")
    public ResponseEntity<String> enviarSolicitudAmistad(@PathVariable Long destinatarioId) {
        try {
            Long remitenteId = amigosService.getCurrentAuthenticatedUserId();
            amigosService.enviarSolicitud(remitenteId, destinatarioId);
            return ResponseEntity.ok("Solicitud de amistad enviada.");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Maneja casos como "Ya son amigos" o "Solicitud duplicada"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud.");
        }
    }

    // --- 3. LISTAR AMIGOS (GET /api/amigos) ---
    @GetMapping
    public ResponseEntity<List<UsuarioMinDTO>> getFriends() {
        try {
            Long currentUserId = amigosService.getCurrentAuthenticatedUserId();
            List<UsuarioMinDTO> friends = amigosService.getUserFriends(currentUserId);
            return ResponseEntity.ok(friends);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- 4. SOLICITUDES PENDIENTES RECIBIDAS (GET /api/amigos/pendientes) ---
    @GetMapping("/pendientes")
    public ResponseEntity<List<UsuarioMinDTO>> getPendingRequests() {
        try {
            Long currentUserId = amigosService.getCurrentAuthenticatedUserId();
            List<UsuarioMinDTO> pendingRequests = amigosService.getPendingFriendRequests(currentUserId);
            return ResponseEntity.ok(pendingRequests);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- 5. ACEPTAR SOLICITUD (POST /api/amigos/aceptar/{solicitudId}) ---
    @PostMapping("/aceptar/{solicitudId}")
    public ResponseEntity<String> aceptarSolicitud(@PathVariable Long solicitudId) {
        try {
            Long currentUserId = amigosService.getCurrentAuthenticatedUserId();
            amigosService.aceptarSolicitud(solicitudId, currentUserId);
            return ResponseEntity.ok("Solicitud aceptada. ¡Ahora son amigos!");
        } catch (SecurityException e) {
            // No autorizado (no eres el destinatario)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            // La solicitud ya no está pendiente
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al aceptar la solicitud.");
        }
    }

    // --- 6. RECHAZAR SOLICITUD (POST /api/amigos/rechazar/{solicitudId}) ---
    @PostMapping("/rechazar/{solicitudId}")
    public ResponseEntity<String> rechazarSolicitud(@PathVariable Long solicitudId) {
        try {
            Long currentUserId = amigosService.getCurrentAuthenticatedUserId();
            amigosService.rechazarSolicitud(solicitudId, currentUserId);
            return ResponseEntity.ok("Solicitud rechazada.");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al rechazar la solicitud.");
        }
    }
}