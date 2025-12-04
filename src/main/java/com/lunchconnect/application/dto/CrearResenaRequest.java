package com.lunchconnect.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearResenaRequest {

    @NotBlank(message = "El texto de la reseña es obligatorio")
    @Size(min = 10, max = 500, message = "La reseña debe tener entre 10 y 500 caracteres")
    private String textoResena;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer calificacion;
}