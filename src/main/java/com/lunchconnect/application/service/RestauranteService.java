package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.RestauranteDTO;
import com.lunchconnect.application.mapper.RestauranteMapper;
import com.lunchconnect.domain.model.Restaurante;
import com.lunchconnect.domain.repository.RestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final RestauranteMapper restauranteMapper;

    public List<RestauranteDTO> obtenerTodos() {
        return restauranteRepository.findAll().stream()
                .map(restauranteMapper::toDTO)
                .collect(Collectors.toList());
    }

    public RestauranteDTO obtenerPorId(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado con id: " + id));
        return restauranteMapper.toDTO(restaurante);
    }

    public List<RestauranteDTO> filtrarRestaurantes(String categoria, String distrito) {
        return restauranteRepository.filtrarRestaurantes(categoria, distrito).stream()
                .map(restauranteMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestauranteDTO> obtenerPorDistrito(String distrito) {
        return restauranteRepository.findByDistrito(distrito).stream()
                .map(restauranteMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestauranteDTO> obtenerPorCategoria(String categoria) {
        return restauranteRepository.findByCategoria(categoria).stream()
                .map(restauranteMapper::toDTO)
                .collect(Collectors.toList());
    }

    public RestauranteDTO crear(RestauranteDTO restauranteDTO) {
        Restaurante restaurante = Restaurante.builder()
                .nombre(restauranteDTO.getNombre())
                .direccion(restauranteDTO.getDireccion())
                .distrito(restauranteDTO.getDistrito())
                .categoria(restauranteDTO.getCategoria())
                .calificacionPromedio(restauranteDTO.getCalificacionPromedio())
                .urlImagen(restauranteDTO.getUrlImagen())
                .capacidadMaxima(restauranteDTO.getCapacidadMaxima())
                .contactoGerente(restauranteDTO.getContactoGerente())
                .build();

        Restaurante guardado = restauranteRepository.save(restaurante);
        return restauranteMapper.toDTO(guardado);
    }

    public void eliminar(Long id) {
        if (!restauranteRepository.existsById(id)) {
            throw new RuntimeException("Restaurante no encontrado");
        }
        restauranteRepository.deleteById(id);
    }
}