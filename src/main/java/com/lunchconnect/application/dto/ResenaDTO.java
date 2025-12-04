package com.lunchconnect.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResenaDTO {
    private Long id;
    private String textoResena;
    private Integer calificacion;
    private LocalDateTime fechaCreacion;

    // Información del usuario
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioTitulo;

    // Información del restaurante
    private Long restauranteId;
    private String restauranteNombre;
}