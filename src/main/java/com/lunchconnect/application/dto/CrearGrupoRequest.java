package com.lunchconnect.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearGrupoRequest {

    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombreGrupo;

    @NotNull(message = "El número máximo de miembros es obligatorio")
    @Min(value = 2, message = "Mínimo 2 personas")
    @Max(value = 10, message = "Máximo 10 personas")
    private Integer maxMiembros;

    @NotNull(message = "La fecha y hora del almuerzo es obligatoria")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime fechaHoraAlmuerzo;

    @NotNull(message = "El restaurante es obligatorio")
    private Long restauranteId;
}