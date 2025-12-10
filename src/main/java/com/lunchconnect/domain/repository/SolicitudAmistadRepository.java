package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.SolicitudAmistad;
import com.lunchconnect.domain.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudAmistadRepository extends JpaRepository<SolicitudAmistad, Long> {

    // Encuentra solicitudes pendientes donde el usuario actual es el destinatario
    List<SolicitudAmistad> findByDestinatario_IdAndEstado(Long destinatarioId, EstadoSolicitud estado);

    // Verifica si ya existe una solicitud PENDIENTE entre dos usuarios (en ambas direcciones)
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SolicitudAmistad s " +
            "WHERE (s.remitente.id = :id1 AND s.destinatario.id = :id2 AND s.estado = 'PENDIENTE') OR " +
            "      (s.remitente.id = :id2 AND s.destinatario.id = :id1 AND s.estado = 'PENDIENTE')")
    boolean existeSolicitudPendienteEntre(@Param("id1") Long id1, @Param("id2") Long id2);
}