package com.lunchconnect.application.service.dto;

// Este DTO se usa para enviar la información mínima de un usuario al frontend
// En el caso de la búsqueda, el campo 'status' indica el estado de amistad.
public record UsuarioMinDTO(
        Long id,
        String name,
        String tag,
        String role,
        String status // Valores posibles: "AMIGO", "SOLICITUD_PENDIENTE", "NINGUNO", "PENDIENTE_RECIBIDA"
) {}