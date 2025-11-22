package com.lunchconnect.application.dto;

import com.lunchconnect.domain.model.Grupo.EstadoGrupo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoDTO {
    private Long id;
    private String nombreGrupo;
    private Integer maxMiembros;
    private LocalDateTime fechaHoraAlmuerzo;
    private EstadoGrupo estado;
    private Integer espaciosDisponibles;
    private Integer participantesCount;

    private Long creadorId;
    private String creadorNombre;
    private String creadorTitulo;

    private Long restauranteId;
    private String restauranteNombre;
    private String restauranteDireccion;
    private String restauranteDistrito;

    private List<UsuarioDTO> participantes;
}