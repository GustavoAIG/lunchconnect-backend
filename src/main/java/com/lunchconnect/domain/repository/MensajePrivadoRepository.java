package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.MensajePrivado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Nota: Tu BD usa id_mensaje como PK.
public interface MensajePrivadoRepository extends JpaRepository<MensajePrivado, Long> {

    // Obtener el historial de mensajes de una conversación
    List<MensajePrivado> findByConversacion_IdConversacionOrderByFechaEnvioAsc(Long idConversacion);
}