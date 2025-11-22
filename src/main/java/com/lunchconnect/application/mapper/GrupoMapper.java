package com.lunchconnect.application.mapper;

import com.lunchconnect.application.dto.GrupoDTO;
import com.lunchconnect.domain.model.Grupo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GrupoMapper {

    private final UsuarioMapper usuarioMapper;

    public GrupoDTO toDTO(Grupo grupo) {
        if (grupo == null) return null;

        return GrupoDTO.builder()
                .id(grupo.getId())
                .nombreGrupo(grupo.getNombreGrupo())
                .maxMiembros(grupo.getMaxMiembros())
                .fechaHoraAlmuerzo(grupo.getFechaHoraAlmuerzo())
                .estado(grupo.getEstado())
                .espaciosDisponibles(grupo.espaciosDisponibles())
                .participantesCount(grupo.getParticipantes().size())
                .creadorId(grupo.getCreador().getId())
                .creadorNombre(grupo.getCreador().getNombreCompleto())
                .creadorTitulo(grupo.getCreador().getTituloPrincipal())
                .restauranteId(grupo.getRestaurante().getId())
                .restauranteNombre(grupo.getRestaurante().getNombre())
                .restauranteDireccion(grupo.getRestaurante().getDireccion())
                .restauranteDistrito(grupo.getRestaurante().getDistrito())
                .build();
    }

    public GrupoDTO toDTOConParticipantes(Grupo grupo) {
        GrupoDTO dto = toDTO(grupo);
        if (dto != null) {
            dto.setParticipantes(
                    grupo.getParticipantes().stream()
                            .map(usuarioMapper::toDTO)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }
}