package com.lunchconnect.application.service;

import com.lunchconnect.domain.enums.EstadoSolicitud;
import com.lunchconnect.domain.model.SolicitudAmistad;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.SolicitudAmistadRepository;
import com.lunchconnect.domain.repository.UsuarioRepository;
import com.lunchconnect.application.service.dto.UsuarioMinDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import com.lunchconnect.infrastructure.security.CustomUserDetailsService;

@Service
@RequiredArgsConstructor
public class AmigosService {

    private final CustomUserDetailsService userDetailsService;
    private final SolicitudAmistadRepository solicitudAmistadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChatPrivadoService chatPrivadoService;

    // -------------------------------------------------------------------------
    // UTILITY: Seguridad y Mapeo
    // -------------------------------------------------------------------------

    /**
     * Obtiene el ID del usuario autenticado actualmente (DEBES ADAPTAR ESTO).
     */
    // Asegúrate de que este método está en AmigosService.java

    // Asegúrate de que este método está en AmigosService.java

    public Long getCurrentAuthenticatedUserId() {

        // 1. Obtener el objeto Principal del contexto (que es el UserDetails)
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            // 2. Extraer el "username" (que es el correo electrónico o nombre de usuario)
            String usernameOrEmail = ((UserDetails) principal).getUsername();

            // 3. Usar tu CustomUserDetailsService para cargar la entidad Usuario y obtener el ID
            //    (Necesitas inyectar el CustomUserDetailsService o UsuarioRepository aquí)

            // --- METODO RECOMENDADO ---

            Usuario usuario = userDetailsService.loadUserEntityByUsername(usernameOrEmail);

            // 4. Devolver el ID real de la entidad
            return usuario.getId();

        } else if (principal instanceof String) {
            // Fallback si el principal es solo una cadena (ej. el username)
            String usernameOrEmail = (String) principal;
            Usuario usuario = userDetailsService.loadUserEntityByUsername(usernameOrEmail);
            return usuario.getId();
        }

        // Si no hay autenticación o es anónima.
        throw new SecurityException("No se encontró un usuario autenticado o el principal es desconocido.");
    }

    /**
     * Busca y valida un usuario por ID.
     */
    private Usuario findUsuarioById(Long userId, String role) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(role + " no encontrado con ID: " + userId));
    }

    /**
     * Mapea el modelo Usuario a un DTO simplificado, usando los campos del Usuario.java proporcionado.
     */
    private UsuarioMinDTO mapToMinDTO(Usuario usuario) {
        // *** CORRECCIÓN APLICADA ***
        // 1. name: Usamos getNombreCompleto()
        String name = usuario.getNombreCompleto();

        // 2. tag: Usamos getNombreUsuario()
        String tag = usuario.getNombreUsuario() != null ? usuario.getNombreUsuario() : "N/A";

        // 3. role: Usamos getTituloPrincipal()
        String role = usuario.getTituloPrincipal() != null ? usuario.getTituloPrincipal() : "General";

        // El status siempre es 'null' aquí y se define en los métodos de servicio (getUserFriends/searchUsers)
        return new UsuarioMinDTO(usuario.getId(), name, tag, role, null);
    }

    // -------------------------------------------------------------------------
    // LÓGICA DE NEGOCIO (FLUJO DE ESCRITURA: Enviar, Aceptar, Rechazar)
    // -------------------------------------------------------------------------

    @Transactional
    public SolicitudAmistad enviarSolicitud(Long remitenteId, Long destinatarioId) {
        if (remitenteId.equals(destinatarioId)) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo.");
        }

        Usuario remitente = findUsuarioById(remitenteId, "Remitente");
        Usuario destinatario = findUsuarioById(destinatarioId, "Destinatario");

        if (solicitudAmistadRepository.existeSolicitudPendienteEntre(remitenteId, destinatarioId)) {
            throw new IllegalStateException("Ya existe una solicitud pendiente entre estos usuarios.");
        }

        if (solicitudAmistadRepository.sonAmigos(remitenteId, destinatarioId)) {
            throw new IllegalStateException("Ya son amigos.");
        }

        SolicitudAmistad solicitud = new SolicitudAmistad();
        solicitud.setRemitente(remitente);
        solicitud.setDestinatario(destinatario);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaEnvio(LocalDateTime.now());

        return solicitudAmistadRepository.save(solicitud);
    }

    @Transactional
    public SolicitudAmistad aceptarSolicitud(Long solicitudId, Long usuarioQueAceptaId) {
        SolicitudAmistad solicitud = solicitudAmistadRepository.findById(solicitudId)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada."));

        if (!solicitud.getDestinatario().getId().equals(usuarioQueAceptaId)) {
            throw new SecurityException("No estás autorizado a responder esta solicitud.");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("La solicitud ya fue respondida.");
        }

        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        SolicitudAmistad solicitudAceptada = solicitudAmistadRepository.save(solicitud);

        // Lógica crítica: Crear el chat 1:1 al hacerse amigos
        chatPrivadoService.getOrCreateConversacion(
                solicitud.getRemitente().getId(),
                solicitud.getDestinatario().getId()
        );

        return solicitudAceptada;
    }

    @Transactional
    public SolicitudAmistad rechazarSolicitud(Long solicitudId, Long usuarioQueRechazaId) {
        SolicitudAmistad solicitud = solicitudAmistadRepository.findById(solicitudId)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada."));

        if (!solicitud.getDestinatario().getId().equals(usuarioQueRechazaId)) {
            throw new SecurityException("No estás autorizado a responder esta solicitud.");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("La solicitud ya fue respondida.");
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());

        return solicitudAmistadRepository.save(solicitud);
    }

    // -------------------------------------------------------------------------
    // LÓGICA DE NEGOCIO (FLUJO DE LECTURA: Amigos, Pendientes, Buscar)
    // -------------------------------------------------------------------------

    /**
     * Obtiene todas las solicitudes de amistad PENDIENTES dirigidas al usuario.
     */
    @Transactional(readOnly = true)
    public List<UsuarioMinDTO> getPendingFriendRequests(Long destinatarioId) {
        // Buscamos las solicitudes y devolvemos los datos del REMITENTE
        return solicitudAmistadRepository
                .findByDestinatario_IdAndEstado(destinatarioId, EstadoSolicitud.PENDIENTE)
                .stream()
                .map(solicitud -> {
                    UsuarioMinDTO dto = mapToMinDTO(solicitud.getRemitente());
                    // Devolvemos el ID de la solicitud en el campo 'id' del DTO,
                    // y el estado "PENDIENTE_RECIBIDA" para el frontend.
                    return new UsuarioMinDTO(solicitud.getId(), dto.name(), dto.tag(), dto.role(), "PENDIENTE_RECIBIDA");
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de usuarios que son amigos del usuario actual.
     */
    @Transactional(readOnly = true)
    public List<UsuarioMinDTO> getUserFriends(Long userId) {
        // Asume que SolicitudAmistadRepository.findAcceptedFriends(userId) está implementado
        // para buscar solicitudes ACEPTADAS donde userId es remitente O destinatario.
        List<SolicitudAmistad> acceptedRequests = solicitudAmistadRepository.findAcceptedFriends(userId);

        return acceptedRequests.stream()
                .map(solicitud -> {
                    // Selecciona el usuario que NO es el usuario actual
                    Usuario friend = solicitud.getRemitente().getId().equals(userId)
                            ? solicitud.getDestinatario()
                            : solicitud.getRemitente();
                    return mapToMinDTO(friend);
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca usuarios por término y determina el estado de amistad.
     */
    @Transactional(readOnly = true)
    public List<UsuarioMinDTO> searchUsers(Long currentUserId, String searchTerm) {
        // 1. Buscar usuarios que coincidan con el término (excluyendo al usuario actual)
        List<Usuario> potentialFriends = usuarioRepository
                .searchByUsernameOrTag(searchTerm) // DEBES IMPLEMENTAR ESTE MÉTODO EN TU REPOSITORY
                .stream()
                .filter(u -> !u.getId().equals(currentUserId)) // Excluirse a sí mismo
                .collect(Collectors.toList());

        if (potentialFriends.isEmpty()) {
            return List.of();
        }

        // 2. Obtener estados de amistad en masa (optimización de DB)
        Set<Long> potentialFriendIds = potentialFriends.stream()
                .map(Usuario::getId)
                .collect(Collectors.toSet());

        // Asume un método en el Repository que retorna [OtroUsuarioId, Estado]
        List<Object[]> friendshipStatuses = solicitudAmistadRepository
                .findFriendshipStatusForUsers(currentUserId, potentialFriendIds);

        var statusMap = friendshipStatuses.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (String) arr[1]
                ));

        // 3. Mapear a DTO con el estado
        return potentialFriends.stream()
                .map(user -> {
                    String status = statusMap.getOrDefault(user.getId(), "NINGUNO");
                    UsuarioMinDTO dto = mapToMinDTO(user);

                    return new UsuarioMinDTO(
                            dto.id(),
                            dto.name(),
                            dto.tag(),
                            dto.role(),
                            status.equals("ACEPTADA") ? "AMIGO" : (status.equals("PENDIENTE") ? "SOLICITUD_PENDIENTE" : "NINGUNO")
                    );
                })
                .collect(Collectors.toList());
    }
}