package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.ConversacionPrivada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// Nota: Tu BD usa id_conversacion como PK.
public interface ConversacionPrivadaRepository extends JpaRepository<ConversacionPrivada, Long> {

    // Buscar conversación sin importar si el usuario1 es A o B.
    @Query("SELECT c FROM ConversacionPrivada c WHERE " +
            "(:usuarioId1 = c.usuarioA.id AND :usuarioId2 = c.usuarioB.id) OR " +
            "(:usuarioId2 = c.usuarioA.id AND :usuarioId1 = c.usuarioB.id)")
    Optional<ConversacionPrivada> findByUsuarios(
            @Param("usuarioId1") Long usuarioId1,
            @Param("usuarioId2") Long usuarioId2);
}