package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.CrearGrupoRequest;
import com.lunchconnect.application.dto.GrupoDTO;
import com.lunchconnect.application.dto.UsuarioDTO;
import com.lunchconnect.application.service.GrupoService;
import com.lunchconnect.infrastructure.security.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class GrupoController {

    private final GrupoService grupoService;
    private final CustomUserDetailsService userDetailsService;

    @Operation(
            summary = "Crear nuevo grupo",
            description = "Crea un nuevo grupo de almuerzo. El usuario autenticado será el creador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grupo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public ResponseEntity<GrupoDTO> crearGrupo(@Valid @RequestBody CrearGrupoRequest request) {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        GrupoDTO grupo = grupoService.crearGrupo(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(grupo);
    }


    @Operation(
            summary = "Listar grupos disponibles",
            description = "Obtiene todos los grupos activos disponibles para unirse"
    )
    @GetMapping
    public ResponseEntity<List<GrupoDTO>> obtenerGruposDisponibles() {
        return ResponseEntity.ok(grupoService.obtenerGruposDisponibles());
    }

    @GetMapping("/paginado")
    public ResponseEntity<Page<GrupoDTO>> obtenerGruposDisponiblesPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaHoraAlmuerzo") String sortBy) {
        return ResponseEntity.ok(grupoService.obtenerGruposDisponiblesPaginados(page, size, sortBy));
    }

    @Operation(
            summary = "Obtener detalle de grupo",
            description = "Obtiene información detallada de un grupo incluyendo participantes"
    )
    @GetMapping("/{id}")
    public ResponseEntity<GrupoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(grupoService.obtenerPorId(id));
    }

    @GetMapping("/mis-grupos")
    public ResponseEntity<List<GrupoDTO>> obtenerMisGrupos() {
        Long usuarioId = obtenerUsuarioIdAutenticado();
        return ResponseEntity.ok(grupoService.obtenerMisGrupos(usuarioId));
    }

    @Operation(
            summary = "Unirse a un grupo",
            description = "El usuario autenticado se une al grupo especificado"
    )
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

    @GetMapping("/buscar")
    @Operation(summary = "Búsqueda avanzada de grupos")
    public ResponseEntity<List<GrupoDTO>> buscarGrupos(
            @RequestParam(required = false) String nombreGrupo,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(grupoService.buscarGrupos(nombreGrupo, distrito, categoria, fechaInicio, fechaFin));
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