package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.UsuarioDTO;
import com.lunchconnect.application.mapper.UsuarioMapper;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.UsuarioRepository;
import com.lunchconnect.infrastructure.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioMock;
    private UsuarioDTO usuarioDTOMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Pérez")
                .correoElectronico("juan@example.com")
                .build();

        usuarioDTOMock = UsuarioDTO.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Pérez")
                .build();
    }

    @Test
    void obtenerPorId_DeberiaRetornarUsuario() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(usuarioMapper.toDTO(usuarioMock)).thenReturn(usuarioDTOMock);

        // Act
        UsuarioDTO resultado = usuarioService.obtenerPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getNombres());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_DeberiaLanzarExcepcionSiNoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            usuarioService.obtenerPorId(999L);
        });
    }
}