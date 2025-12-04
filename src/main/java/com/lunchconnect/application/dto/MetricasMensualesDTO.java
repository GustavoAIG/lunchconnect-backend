package com.lunchconnect.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricasMensualesDTO {
    private String mes;
    private Long gruposCreados;
    private Long usuariosRegistrados;
    private Long participantesTotales;
}