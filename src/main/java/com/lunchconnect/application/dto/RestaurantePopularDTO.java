package com.lunchconnect.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantePopularDTO {
    private Long restauranteId;
    private String nombre;
    private String distrito;
    private String categoria;
    private Long cantidadGrupos;
    private Long cantidadParticipantes;
}