package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.CrearGrupoRequest;
import com.lunchconnect.application.dto.GrupoDTO;
import com.lunchconnect.application.dto.UsuarioDTO;
import com.lunchconnect.application.service.GrupoService;
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
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class GrupoController {

    private final GrupoService grupoService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping
    public ResponseEntity<GrupoDTO> crearGrupo(@Valid @RequestBody CrearGrupoRequest request) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        GrupoDTO grupo = grupoService.crearGrupo(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(grupo);
    }

    @GetMapping
    public ResponseEntity<List<GrupoDTO>> obtenerGruposDisponibles() {
        return ResponseEntity.ok(grupoService.obtenerGruposDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(grupoService.obtenerPorId(id));
    }

    @GetMapping("/mis-grupos")
    public ResponseEntity<List<GrupoDTO>> obtenerMisGrupos() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        return ResponseEntity.ok(grupoService.obtenerMisGrupos(usuarioId));
    }

    @PostMapping("/{id}/unirse")
    public ResponseEntity<GrupoDTO> unirseAGrupo(@PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        GrupoDTO grupo = grupoService.unirseAGrupo(id, usuarioId);
        return ResponseEntity.ok(grupo);
    }

    @DeleteMapping("/{id}/salir")
    public ResponseEntity<Void> salirDelGrupo(@PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        grupoService.salirDelGrupo(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/participantes")
    public ResponseEntity<List<UsuarioDTO>> obtenerParticipantes(@PathVariable Long id) {
        return ResponseEntity.ok(grupoService.obtenerParticipantes(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGrupo(@PathVariable Long id) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        grupoService.eliminarGrupo(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Long obtenerUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Si no hay autenticación o es anónimo, lanza excepción
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String correo = authentication.getName();
        return userDetailsService.loadUserEntityByUsername(correo).getId();
    }
}