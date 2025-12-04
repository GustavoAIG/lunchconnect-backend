package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.*;
import com.lunchconnect.application.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasDTO> obtenerEstadisticas() {
        return ResponseEntity.ok(adminService.obtenerEstadisticas());
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> obtenerTodosLosUsuarios() {
        return ResponseEntity.ok(adminService.obtenerTodosLosUsuarios());
    }

    @GetMapping("/grupos")
    public ResponseEntity<List<GrupoDTO>> obtenerTodosLosGrupos() {
        return ResponseEntity.ok(adminService.obtenerTodosLosGrupos());
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        adminService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuarios/{id}/asignar-admin")
    public ResponseEntity<Void> asignarRolAdmin(@PathVariable Long id) {
        adminService.asignarRolAdmin(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/usuarios/{id}/remover-admin")
    public ResponseEntity<Void> removerRolAdmin(@PathVariable Long id) {
        adminService.removerRolAdmin(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener dashboard completo con todas las métricas")
    public ResponseEntity<Map<String, Object>> obtenerDashboard() {
        return ResponseEntity.ok(adminService.obtenerDashboard());
    }

    @GetMapping("/restaurantes-populares")
    @Operation(summary = "Obtener restaurantes más populares")
    public ResponseEntity<List<RestaurantePopularDTO>> obtenerRestaurantesPopulares(
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(adminService.obtenerRestaurantesMasPopulares(limite));
    }

    @GetMapping("/metricas-mensuales")
    @Operation(summary = "Obtener métricas de los últimos meses")
    public ResponseEntity<List<MetricasMensualesDTO>> obtenerMetricasMensuales(
            @RequestParam(defaultValue = "6") int meses) {
        return ResponseEntity.ok(adminService.obtenerMetricasMensuales(meses));
    }
}