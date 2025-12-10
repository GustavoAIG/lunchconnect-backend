package com.lunchconnect.application.service.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UsuarioAdminDTO(
        Long id,
        String nombreCompleto,
        String nombreUsuario,
        String correoElectronico,
        String rubroProfesional,
        String tituloPrincipal,
        Set<String> roles,
        LocalDateTime fechaCreacion
) {}