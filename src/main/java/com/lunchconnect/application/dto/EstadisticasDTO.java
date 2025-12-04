package com.lunchconnect.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasDTO {
    private Long totalUsuarios;
    private Long totalGrupos;
    private Long totalRestaurantes;
    private Long gruposActivos;
    private Long gruposCompletados;
    private Long usuariosActivos; // Usuarios con al menos 1 grupo
    private Double promedioParticipantesPorGrupo;
    private String restauranteMasPopular;
}