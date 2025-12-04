package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.RestauranteDTO;
import com.lunchconnect.application.mapper.RestauranteMapper;
import com.lunchconnect.domain.model.Restaurante;
import com.lunchconnect.domain.repository.RestauranteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestauranteServiceTest {

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private RestauranteMapper restauranteMapper;

    @InjectMocks
    private RestauranteService restauranteService;

    private Restaurante restaurante1;
    private Restaurante restaurante2;

    @BeforeEach
    void setUp() {
        restaurante1 = Restaurante.builder()
                .id(1L)
                .nombre("Restaurante 1")
                .distrito("Miraflores")
                .categoria("Chifa")
                .calificacionPromedio(new BigDecimal("4.5"))
                .build();

        restaurante2 = Restaurante.builder()
                .id(2L)
                .nombre("Restaurante 2")
                .distrito("San Isidro")
                .categoria("Criolla")
                .build();
    }

    @Test
    void obtenerTodos_DeberiaRetornarListaDeRestaurantes() {
        // Arrange
        when(restauranteRepository.findAll()).thenReturn(Arrays.asList(restaurante1, restaurante2));
        when(restauranteMapper.toDTO(any())).thenReturn(new RestauranteDTO());

        // Act
        List<RestauranteDTO> resultado = restauranteService.obtenerTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(restauranteRepository, times(1)).findAll();
    }

    @Test
    void filtrarRestaurantes_DeberiaRetornarRestaurantesFiltrados() {
        // Arrange
        when(restauranteRepository.filtrarRestaurantes("Chifa", "Miraflores"))
                .thenReturn(Arrays.asList(restaurante1));
        when(restauranteMapper.toDTO(restaurante1)).thenReturn(new RestauranteDTO());

        // Act
        List<RestauranteDTO> resultado = restauranteService.filtrarRestaurantes("Chifa", "Miraflores");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(restauranteRepository, times(1)).filtrarRestaurantes("Chifa", "Miraflores");
    }
}