package com.lunchconnect.application.mapper;

import com.lunchconnect.application.dto.ResenaDTO;
import com.lunchconnect.domain.model.Resena;
import org.springframework.stereotype.Component;

@Component
public class ResenaMapper {

    public ResenaDTO toDTO(Resena resena) {
        if (resena == null) return null;

        return ResenaDTO.builder()
                .id(resena.getId())
                .textoResena(resena.getTextoResena())
                .calificacion(resena.getCalificacion())
                .fechaCreacion(resena.getFechaCreacion())
                .usuarioId(resena.getUsuario().getId())
                .usuarioNombre(resena.getUsuario().getNombreCompleto())
                .usuarioTitulo(resena.getUsuario().getTituloPrincipal())
                .restauranteId(resena.getRestaurante().getId())
                .restauranteNombre(resena.getRestaurante().getNombre())
                .build();
    }
}