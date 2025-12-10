package com.lunchconnect.application.service;

import com.lunchconnect.domain.enums.EstadoSolicitud;
import com.lunchconnect.domain.model.SolicitudAmistad;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.SolicitudAmistadRepository;
import com.lunchconnect.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AmigosService {

    private final SolicitudAmistadRepository solicitudAmistadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChatPrivadoService chatPrivadoService; // Servicio inyectado para crear la conversación

    /**
     * Busca y valida un usuario por ID.
     */
    private Usuario findUsuarioById(Long userId, String role) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(role + " no encontrado con ID: " + userId));
    }

    /**
     * Envía una nueva solicitud de amistad.
     * @param remitenteId ID del usuario que envía.
     * @param destinatarioId ID del usuario que recibe.
     * @return La solicitud de amistad guardada.
     */
    @Transactional
    public SolicitudAmistad enviarSolicitud(Long remitenteId, Long destinatarioId) {
        if (remitenteId.equals(destinatarioId)) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo.");
        }

        Usuario remitente = findUsuarioById(remitenteId, "Remitente");
        Usuario destinatario = findUsuarioById(destinatarioId, "Destinatario");

        // 1. Prevenir si ya existe una solicitud PENDIENTE (en cualquier dirección)
        if (solicitudAmistadRepository.existeSolicitudPendienteEntre(remitenteId, destinatarioId)) {
            throw new IllegalStateException("Ya existe una solicitud pendiente entre estos usuarios.");
        }

        // 2. Prevenir si ya son amigos (asumiendo que ACCEPTED significa que ya se creó el chat)
        // Podrías añadir una lógica más robusta aquí si es necesario.

        // 3. Crear y guardar nueva solicitud
        SolicitudAmistad solicitud = new SolicitudAmistad();
        solicitud.setRemitente(remitente);
        solicitud.setDestinatario(destinatario);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaEnvio(LocalDateTime.now());

        return solicitudAmistadRepository.save(solicitud);
    }

    /**
     * Acepta una solicitud de amistad.
     * CRÍTICO: Al aceptar, se garantiza la existencia de la ConversacionPrivada.
     * @param solicitudId ID de la solicitud a aceptar.
     * @param usuarioQueAceptaId ID del usuario que realiza la acción (debe ser el destinatario).
     * @return La solicitud actualizada.
     */
    @Transactional
    public SolicitudAmistad aceptarSolicitud(Long solicitudId, Long usuarioQueAceptaId) {
        SolicitudAmistad solicitud = solicitudAmistadRepository.findById(solicitudId)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada."));

        // 1. Validación de Seguridad y Estado
        if (!solicitud.getDestinatario().getId().equals(usuarioQueAceptaId)) {
            throw new SecurityException("No estás autorizado a responder esta solicitud.");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("La solicitud ya fue respondida.");
        }

        // 2. Actualizar estado
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        SolicitudAmistad solicitudAceptada = solicitudAmistadRepository.save(solicitud);

        // 3. CREAR OBTENER LA CONVERSACIÓN PRIVADA
        // Llama al servicio de chat para asegurarse de que el registro 1:1 exista.
        chatPrivadoService.getOrCreateConversacion(
                solicitud.getRemitente().getId(),
                solicitud.getDestinatario().getId()
        );

        return solicitudAceptada;
    }

    /**
     * Marca una solicitud como RECHAZADA.
     * @param solicitudId ID de la solicitud.
     * @param usuarioQueRechazaId ID del usuario que rechaza.
     * @return La solicitud actualizada.
     */
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

    /**
     * Obtiene todas las solicitudes de amistad PENDIENTES dirigidas al usuario.
     */
    @Transactional(readOnly = true)
    public List<SolicitudAmistad> obtenerPendientesRecibidas(Long usuarioId) {
        return solicitudAmistadRepository.findByDestinatario_IdAndEstado(usuarioId, EstadoSolicitud.PENDIENTE);
    }

    // 💡 NOTA: Obtener la lista completa de amigos requiere una consulta más compleja
    // que une las solicitudes donde el estado es ACEPTADA, buscando el usuario en remitente O destinatario.
}