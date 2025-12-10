package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.SolicitudAmistad;
import com.lunchconnect.domain.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SolicitudAmistadRepository extends JpaRepository<SolicitudAmistad, Long> {

    // Encuentra solicitudes pendientes donde el usuario actual es el destinatario
    List<SolicitudAmistad> findByDestinatario_IdAndEstado(Long destinatarioId, EstadoSolicitud estado);

    // Verifica si ya existe una solicitud PENDIENTE entre dos usuarios (en ambas direcciones)
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SolicitudAmistad s " +
            "WHERE s.estado = 'PENDIENTE' AND " +
            "((s.remitente.id = :id1 AND s.destinatario.id = :id2) OR " +
            " (s.remitente.id = :id2 AND s.destinatario.id = :id1))")
    boolean existeSolicitudPendienteEntre(@Param("id1") Long id1, @Param("id2") Long id2);

    /**
     * Verifica si dos usuarios son amigos (existe una solicitud ACEPTADA en cualquier dirección).
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SolicitudAmistad s " +
            "WHERE s.estado = 'ACEPTADA' AND " +
            "((s.remitente.id = :id1 AND s.destinatario.id = :id2) OR " +
            " (s.remitente.id = :id2 AND s.destinatario.id = :id1))")
    boolean sonAmigos(@Param("id1") Long id1, @Param("id2") Long id2);

    /**
     * Encuentra todas las solicitudes ACEPTADAS donde el usuario es remitente o destinatario.
     * Utilizado para cargar la lista de amigos.
     */
    @Query("SELECT s FROM SolicitudAmistad s " +
            "WHERE s.estado = 'ACEPTADA' AND " +
            "(s.remitente.id = :userId OR s.destinatario.id = :userId)")
    List<SolicitudAmistad> findAcceptedFriends(@Param("userId") Long userId);

    /**
     * Obtiene el estado de amistad (ACEPTADA, PENDIENTE, RECHAZADA) entre el usuario actual y un conjunto de otros usuarios.
     * Utilizado para optimizar la búsqueda de usuarios.
     *
     * Retorna una lista de arrays: [ID_del_otro_usuario, Estado_de_la_solicitud]
     */
    @Query("SELECT " +
            "CASE WHEN s.remitente.id = :currentUserId THEN s.destinatario.id ELSE s.remitente.id END, " +
            "CAST(s.estado AS string) " +
            "FROM SolicitudAmistad s " +
            "WHERE s.estado IN ('ACEPTADA', 'PENDIENTE') AND " +
            "((s.remitente.id = :currentUserId AND s.destinatario.id IN :potentialFriendIds) OR " +
            " (s.destinatario.id = :currentUserId AND s.remitente.id IN :potentialFriendIds))")
    List<Object[]> findFriendshipStatusForUsers(@Param("currentUserId") Long currentUserId, @Param("potentialFriendIds") Set<Long> potentialFriendIds);
}