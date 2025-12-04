package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.CrearResenaRequest;
import com.lunchconnect.application.dto.ResenaDTO;
import com.lunchconnect.application.service.ResenaService;
import com.lunchconnect.infrastructure.security.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ResenaController {

    private final ResenaService resenaService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/restaurante/{restauranteId}")
    public ResponseEntity<ResenaDTO> crearResena(
            @PathVariable Long restauranteId,
            @Valid @RequestBody CrearResenaRequest request) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        ResenaDTO resena = resenaService.crearResena(restauranteId, request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resena);
    }

    @GetMapping("/restaurante/{restauranteId}")
    public ResponseEntity<List<ResenaDTO>> obtenerResenasPorRestaurante(@PathVariable Long restauranteId) {
        return ResponseEntity.ok(resenaService.obtenerResenasPorRestaurante(restauranteId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ResenaDTO>> obtenerResenasPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(resenaService.obtenerResenasPorUsuario(usuarioId));
    }

    @GetMapping("/mis-resenas")
    public ResponseEntity<List<ResenaDTO>> obtenerMisResenas() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        return ResponseEntity.ok(resenaService.obtenerResenasPorUsuario(usuarioId));
    }

    @PutMapping("/{resenaId}")
    public ResponseEntity<ResenaDTO> actualizarResena(
            @PathVariable Long resenaId,
            @Valid @RequestBody CrearResenaRequest request) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        ResenaDTO resena = resenaService.actualizarResena(resenaId, request, usuarioId);
        return ResponseEntity.ok(resena);
    }

    @DeleteMapping("/{resenaId}")
    public ResponseEntity<Void> eliminarResena(@PathVariable Long resenaId) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        resenaService.eliminarResena(resenaId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Long obtenerUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String correo = authentication.getName();
        return userDetailsService.loadUserEntityByUsername(correo).getId();
    }
}