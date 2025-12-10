package com.lunchconnect.presentation.controller;

// Importaciones necesarias y corregidas
import com.lunchconnect.application.service.dto.UsuarioAdminDTO; // DTO REAL del servicio
import com.lunchconnect.application.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set; // Necesario para Set<String> roles

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
// Asegura que solo usuarios con el rol 'ADMIN' puedan acceder a este controlador
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ----------------------------------------------------------------------------------
    // GESTIÓN DE USUARIOS
    // ----------------------------------------------------------------------------------

    @GetMapping("/usuarios")
    @Operation(summary = "Obtener la lista completa de usuarios con detalles administrativos")
    public ResponseEntity<List<UsuarioAdminDTO>> obtenerTodosLosUsuarios() {
        // Usa el método implementado: getAllUsuarios
        return ResponseEntity.ok(adminService.getAllUsuarios());
    }

    @DeleteMapping("/usuarios/{id}")
    @Operation(summary = "Elimina un usuario por ID")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        // Usa el método implementado: deleteUsuario
        adminService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuarios/{id}/asignar-admin")
    @Operation(summary = "Asigna el rol 'ADMIN' a un usuario")
    public ResponseEntity<Void> asignarRolAdmin(@PathVariable Long id) {
        // Lógica de roles: Usamos el método genérico updateUsuarioRoles.
        // Asumimos que los roles existentes son preservados, y se añade 'ADMIN'.
        // NOTA: La lógica real de obtener roles y añadir/remover DEBERÍA estar dentro del AdminService.
        // Aquí pasamos el set fijo por simplicidad.

        // Simulación: Asigna un set que incluye "USER" y "ADMIN"
        adminService.updateUsuarioRoles(id, Set.of("USER", "ADMIN"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/usuarios/{id}/remover-admin")
    @Operation(summary = "Remueve el rol 'ADMIN' de un usuario")
    public ResponseEntity<Void> removerRolAdmin(@PathVariable Long id) {
        // Simulación: Asigna un set que solo incluye "USER"
        adminService.updateUsuarioRoles(id, Set.of("USER"));
        return ResponseEntity.ok().build();
    }

    // ----------------------------------------------------------------------------------
    // FUNCIONALIDADES DE DASHBOARD (Requieren Implementación adicional en AdminService)
    // ----------------------------------------------------------------------------------

    /*
     * NOTA: Los siguientes métodos están comentados o simplificados porque
     * requieren la implementación de DTOs como EstadisticasDTO, GrupoDTO, etc.,
     * y métodos complejos en el AdminService que aún no has definido.
     * Solo se deja el endpoint de dashboard como ejemplo.
     */

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener dashboard completo con todas las métricas")
    public ResponseEntity<Map<String, Object>> obtenerDashboard() {
        // DEBES implementar adminService.obtenerDashboard()
        // Por ahora, retornamos un placeholder si el método no existe.
        // return ResponseEntity.ok(adminService.obtenerDashboard());
        return ResponseEntity.ok(Map.of("message", "Dashboard endpoint implementado, falta la lógica en el servicio."));
    }



    // @GetMapping("/estadisticas")
    // public ResponseEntity<EstadisticasDTO> obtenerEstadisticas() {
    //     // DEBES implementar EstadisticasDTO y adminService.obtenerEstadisticas()
    //     return ResponseEntity.status(501).build(); // 501 Not Implemented
    // }

    // @GetMapping("/grupos")
    // public ResponseEntity<List<GrupoDTO>> obtenerTodosLosGrupos() {
    //     // DEBES implementar GrupoDTO y adminService.obtenerTodosLosGrupos()
    //     return ResponseEntity.status(501).build(); // 501 Not Implemented
    // }

    // ... y el resto de los endpoints de métricas ...

}