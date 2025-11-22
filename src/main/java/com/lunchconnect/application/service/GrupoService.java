package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.CrearGrupoRequest;
import com.lunchconnect.application.dto.GrupoDTO;
import com.lunchconnect.application.dto.UsuarioDTO;
import com.lunchconnect.application.mapper.GrupoMapper;
import com.lunchconnect.application.mapper.UsuarioMapper;
import com.lunchconnect.domain.model.Grupo;
import com.lunchconnect.domain.model.Grupo.EstadoGrupo;
import com.lunchconnect.domain.model.Restaurante;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.GrupoRepository;
import com.lunchconnect.domain.repository.RestauranteRepository;
import com.lunchconnect.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final GrupoMapper grupoMapper;
    private final UsuarioMapper usuarioMapper;


    public GrupoDTO crearGrupo(CrearGrupoRequest request, Long usuarioId) {

        Usuario creador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        Restaurante restaurante = restauranteRepository.findById(request.getRestauranteId())
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));


        if (request.getFechaHoraAlmuerzo().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La fecha del almuerzo debe ser futura");
        }


        Grupo grupo = Grupo.builder()
                .nombreGrupo(request.getNombreGrupo())
                .maxMiembros(request.getMaxMiembros())
                .fechaHoraAlmuerzo(request.getFechaHoraAlmuerzo())
                .restaurante(restaurante)
                .creador(creador)
                .estado(EstadoGrupo.ACTIVO)
                .build();

        // El creador se une automáticamente
        grupo.agregarParticipante(creador);

        Grupo guardado = grupoRepository.save(grupo);
        return grupoMapper.toDTO(guardado);
    }

    public List<GrupoDTO> obtenerGruposDisponibles() {
        return grupoRepository.findGruposDisponibles(EstadoGrupo.ACTIVO, LocalDateTime.now())
                .stream()
                .map(grupoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public GrupoDTO obtenerPorId(Long id) {
        Grupo grupo = grupoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado con id: " + id));
        return grupoMapper.toDTOConParticipantes(grupo);
    }

    public List<GrupoDTO> obtenerMisGrupos(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return grupoRepository.findMisGrupos(usuarioId).stream()
                .map(grupoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public GrupoDTO unirseAGrupo(Long grupoId, Long usuarioId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        if (grupo.getEstado() != EstadoGrupo.ACTIVO) {
            throw new RuntimeException("El grupo no está disponible para unirse");
        }


        if (grupo.estaLleno()) {
            throw new RuntimeException("El grupo está lleno");
        }


        if (grupo.getParticipantes().contains(usuario)) {
            throw new RuntimeException("Ya estás en este grupo");
        }


        grupo.agregarParticipante(usuario);
        Grupo actualizado = grupoRepository.save(grupo);

        return grupoMapper.toDTO(actualizado);
    }

    public void salirDelGrupo(Long grupoId, Long usuarioId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        if (!grupo.getParticipantes().contains(usuario)) {
            throw new RuntimeException("No estás en este grupo");
        }

        // El creador no puede salir si hay otros participantes
        if (grupo.getCreador().equals(usuario) && grupo.getParticipantes().size() > 1) {
            throw new RuntimeException("El creador no puede salir mientras haya otros participantes");
        }

        grupo.eliminarParticipante(usuario);
        grupoRepository.save(grupo);
    }

    public List<UsuarioDTO> obtenerParticipantes(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        return grupo.getParticipantes().stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void eliminarGrupo(Long grupoId, Long usuarioId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));


        if (!grupo.getCreador().getId().equals(usuarioId)) {
            throw new RuntimeException("Solo el creador puede eliminar el grupo");
        }

        grupoRepository.delete(grupo);
    }
}