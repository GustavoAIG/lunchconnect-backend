package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.CrearGrupoRequest;
import com.lunchconnect.application.dto.GrupoDTO;
import com.lunchconnect.application.mapper.GrupoMapper;
import com.lunchconnect.application.mapper.UsuarioMapper;
import com.lunchconnect.domain.model.Grupo;
import com.lunchconnect.domain.model.Restaurante;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.GrupoRepository;
import com.lunchconnect.domain.repository.RestauranteRepository;
import com.lunchconnect.domain.repository.UsuarioRepository;
import com.lunchconnect.infrastructure.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private GrupoMapper grupoMapper;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private GrupoService grupoService;

    private Usuario usuarioMock;
    private Restaurante restauranteMock;
    private Grupo grupoMock;
    private GrupoDTO grupoDTOMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Pérez")
                .correoElectronico("juan@example.com")
                .tituloPrincipal("Ingeniero")
                .rubroProfesional("Tecnología")
                .build();

        restauranteMock = Restaurante.builder()
                .id(1L)
                .nombre("Restaurante Test")
                .direccion("Calle 123")
                .distrito("Miraflores")
                .categoria("Chifa")
                .capacidadMaxima(50)
                .build();

        grupoMock = Grupo.builder()
                .id(1L)
                .nombreGrupo("Almuerzo Test")
                .maxMiembros(5)
                .fechaHoraAlmuerzo(LocalDateTime.now().plusDays(1))
                .restaurante(restauranteMock)
                .creador(usuarioMock)
                .estado(Grupo.EstadoGrupo.ACTIVO)
                .build();

        grupoDTOMock = GrupoDTO.builder()
                .id(1L)
                .nombreGrupo("Almuerzo Test")
                .maxMiembros(5)
                .build();
    }

    @Test
    void crearGrupo_DeberiaCrearGrupoExitosamente() {
        // Arrange
        CrearGrupoRequest request = new CrearGrupoRequest();
        request.setNombreGrupo("Almuerzo Test");
        request.setMaxMiembros(5);
        request.setFechaHoraAlmuerzo(LocalDateTime.now().plusDays(1));
        request.setRestauranteId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restauranteMock));
        when(grupoRepository.save(any(Grupo.class))).thenReturn(grupoMock);
        when(grupoMapper.toDTO(any(Grupo.class))).thenReturn(grupoDTOMock);

        // Act
        GrupoDTO resultado = grupoService.crearGrupo(request, 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Almuerzo Test", resultado.getNombreGrupo());
        verify(grupoRepository, times(1)).save(any(Grupo.class));
    }

    @Test
    void crearGrupo_DeberiaLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        CrearGrupoRequest request = new CrearGrupoRequest();
        request.setRestauranteId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            grupoService.crearGrupo(request, 1L);
        });

        verify(grupoRepository, never()).save(any());
    }

    @Test
    void crearGrupo_DeberiaLanzarExcepcionSiRestauranteNoExiste() {
        // Arrange
        CrearGrupoRequest request = new CrearGrupoRequest();
        request.setRestauranteId(999L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(restauranteRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            grupoService.crearGrupo(request, 1L);
        });

        verify(grupoRepository, never()).save(any());
    }

    @Test
    void unirseAGrupo_DeberiaAgregarParticipanteExitosamente() {
        // Arrange
        Usuario nuevoUsuario = Usuario.builder()
                .id(2L)
                .nombres("María")
                .apellidos("García")
                .build();

        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupoMock));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(nuevoUsuario));
        when(grupoRepository.save(any(Grupo.class))).thenReturn(grupoMock);
        when(grupoMapper.toDTO(any(Grupo.class))).thenReturn(grupoDTOMock);

        // Act
        GrupoDTO resultado = grupoService.unirseAGrupo(1L, 2L);

        // Assert
        assertNotNull(resultado);
        verify(grupoRepository, times(1)).save(any(Grupo.class));
    }

    @Test
    void unirseAGrupo_DeberiaLanzarExcepcionSiGrupoNoExiste() {
        // Arrange
        when(grupoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            grupoService.unirseAGrupo(999L, 2L);
        });
    }
}