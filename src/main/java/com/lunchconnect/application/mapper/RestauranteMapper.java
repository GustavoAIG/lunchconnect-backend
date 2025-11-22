package com.lunchconnect.application.mapper;

import com.lunchconnect.application.dto.RestauranteDTO;
import com.lunchconnect.domain.model.Restaurante;
import org.springframework.stereotype.Component;

@Component
public class RestauranteMapper {

    public RestauranteDTO toDTO(Restaurante restaurante) {
        if (restaurante == null) return null;

        return RestauranteDTO.builder()
                .id(restaurante.getId())
                .nombre(restaurante.getNombre())
                .direccion(restaurante.getDireccion())
                .distrito(restaurante.getDistrito())
                .categoria(restaurante.getCategoria())
                .calificacionPromedio(restaurante.getCalificacionPromedio())
                .urlImagen(restaurante.getUrlImagen())
                .capacidadMaxima(restaurante.getCapacidadMaxima())
                .contactoGerente(restaurante.getContactoGerente())
                .build();
    }
}