package com.lunchconnect.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestauranteDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private String distrito;
    private String categoria;
    private BigDecimal calificacionPromedio;
    private String urlImagen;
    private Integer capacidadMaxima;
    private String contactoGerente;
}