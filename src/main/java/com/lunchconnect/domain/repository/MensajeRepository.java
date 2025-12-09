package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /**
     * Recupera el historial de mensajes para un grupo específico, ordenado por fecha de envío.
     * @param grupoId ID del grupo.
     * @return Lista de mensajes.
     */
    List<Mensaje> findByGrupoIdOrderByFechaEnvioAsc(Long grupoId);

    // Si quisieras paginación:
    // Page<Mensaje> findByGrupoIdOrderByFechaEnvioDesc(Long grupoId, Pageable pageable);
}