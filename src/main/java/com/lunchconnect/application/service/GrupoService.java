package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.CrearGrupoRequest;
import com.lunchconnect.application.dto.GrupoDTO;
import com.lunchconnect.application.dto.UsuarioDTO;
import com.lunchconnect.application.mapper.GrupoMapper;
import com.lunchconnect.application.mapper.UsuarioMapper;
import com.lunchconnect.domain.model.Grupo;
import com.lunchconnect.domain.model.Grupo.EstadoGrupo;
import com.lunchconnect.domain.model.Mensaje;
import com.lunchconnect.domain.model.Restaurante;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.GrupoRepository;
import com.lunchconnect.domain.repository.MensajeRepository;
import com.lunchconnect.domain.repository.RestauranteRepository;
import com.lunchconnect.domain.repository.UsuarioRepository;
import com.lunchconnect.infrastructure.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.lunchconnect.application.dto.ChatMessage; // Importar el DTO
import com.lunchconnect.application.mapper.MensajeMapper; // Importar el nuevo Mapper


@Service
@RequiredArgsConstructor
@Transactional
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final GrupoMapper grupoMapper;
    private final UsuarioMapper usuarioMapper;
    private final ChatService chatService;
    private final MensajeRepository mensajeRepository;
    private final MensajeMapper mensajeMapper; // Necesitas un nuevo Mapper

    public GrupoDTO crearGrupo(CrearGrupoRequest request, Long usuarioId) {

        Usuario creador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));


        Restaurante restaurante = restauranteRepository.findById(request.getRestauranteId())
                .orElseThrow(() -> new NotFoundException("Restaurante no encontrado"));


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
        Long grupoId = guardado.getId();
        // ----------------------------------------------------------------
        // 💡 MODIFICACIÓN: Crear el chat usando el ID LONG REAL del grupo
        // ----------------------------------------------------------------
        // Pasamos el ID del grupo como referencia del chat room.
        String chatRoomId = grupoId.toString();

        // 💡 LLAMADA AL SERVICE: (La implementación de ChatService no necesita hacer nada aquí,
        // solo se necesita la llamada para mantener la abstracción, pero ya tenemos el ID)
        // chatService.createGroupChat(guardado.getNombreGrupo(), List.of(creador.getId()));

        // Asignar el ID Long del grupo como el ChatRoomId
        guardado.setChatRoomId(chatRoomId);
        // ----------------------------------------------------------------

        // Volver a guardar el grupo (si es necesario por la asignación de chatRoomId)
    // Si la transacción está activa, el primer save debería manejarlo, pero un nuevo save es más seguro aquí:
        grupoRepository.save(guardado);

        return grupoMapper.toDTO(guardado);
    }

    public List<GrupoDTO> obtenerGruposDisponibles() {
        return grupoRepository.findGruposDisponibles(EstadoGrupo.ACTIVO, LocalDateTime.now())
                .stream()
                .map(grupoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Page<GrupoDTO> obtenerGruposDisponiblesPaginados(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        Page<Grupo> gruposPage = grupoRepository.findGruposDisponibles(
                EstadoGrupo.ACTIVO,
                LocalDateTime.now(),
                pageable
        );

        return gruposPage.map(grupoMapper::toDTO);
    }

    public GrupoDTO obtenerPorId(Long id) {
        Grupo grupo = grupoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grupo no encontrado con id: " + id));
        return grupoMapper.toDTOConParticipantes(grupo);
    }

    public List<GrupoDTO> obtenerMisGrupos(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new NotFoundException("Usuario no encontrado");
        }
        return grupoRepository.findMisGrupos(usuarioId).stream()
                .map(grupoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public GrupoDTO unirseAGrupo(Long grupoId, Long usuarioId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));


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

        // ----------------------------------------------------------------
        // 💡 PASO CLAVE 2: AÑADIR USUARIO AL CHAT
        if (actualizado.getChatRoomId() != null) {
            chatService.addUserToChat(actualizado.getChatRoomId(), usuario.getId());
        }
        // ----------------------------------------------------------------

        return grupoMapper.toDTO(actualizado);
    }

    public void salirDelGrupo(Long grupoId, Long usuarioId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));


        if (!grupo.getParticipantes().contains(usuario)) {
            throw new RuntimeException("No estás en este grupo");
        }

        // El creador no puede salir si hay otros participantes
        if (grupo.getCreador().equals(usuario) && grupo.getParticipantes().size() > 1) {
            throw new RuntimeException("El creador no puede salir mientras haya otros participantes");
        }

        grupo.eliminarParticipante(usuario);
        grupoRepository.save(grupo);

        // ----------------------------------------------------------------
        // 💡 PASO CLAVE 3: REMOVER USUARIO DEL CHAT
        if (grupo.getChatRoomId() != null) {
            chatService.removeUserFromChat(grupo.getChatRoomId(), usuario.getId());
        }
        // ----------------------------------------------------------------
    }

    public List<UsuarioDTO> obtenerParticipantes(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo no encontrado"));

        return grupo.getParticipantes().stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void eliminarGrupo(Long grupoId, Long usuarioId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo no encontrado"));


        if (!grupo.getCreador().getId().equals(usuarioId)) {
            throw new RuntimeException("Solo el creador puede eliminar el grupo");
        }

        // ----------------------------------------------------------------
        // 💡 PASO CLAVE 4: ELIMINAR EL CHAT
        if (grupo.getChatRoomId() != null) {
            chatService.deleteGroupChat(grupo.getChatRoomId());
        }
        // ----------------------------------------------------------------

        grupoRepository.delete(grupo);
    }

    public List<GrupoDTO> buscarGrupos(String nombreGrupo, String distrito, String categoria,
                                       LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return grupoRepository.buscarGrupos(nombreGrupo, distrito, categoria, fechaInicio, fechaFin)
                .stream()
                .map(grupoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true) // Es solo lectura, optimiza rendimiento
    public List<ChatMessage> obtenerHistorialChat(Long grupoId) {
        // Validación: Asegurarse de que el grupo existe
        if (!grupoRepository.existsById(grupoId)) {
            throw new NotFoundException("Grupo no encontrado con id: " + grupoId);
        }

        // 1. Obtener mensajes persistidos del repositorio (ordenado por fecha)
        List<Mensaje> mensajes = mensajeRepository.findByGrupoIdOrderByFechaEnvioAsc(grupoId);

        // 2. Mapear la lista de entidades a la lista de DTOs usando el mapper
        return mensajes.stream()
                .map(mensajeMapper::toChatMessage)
                .collect(Collectors.toList());
    }
}