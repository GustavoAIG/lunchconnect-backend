package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.Grupo;
import com.lunchconnect.domain.model.Grupo.EstadoGrupo;
import com.lunchconnect.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    List<Grupo> findByEstado(EstadoGrupo estado);

    List<Grupo> findByCreador(Usuario creador);

    @Query("SELECT g FROM Grupo g WHERE :usuario MEMBER OF g.participantes")
    List<Grupo> findGruposByParticipante(@Param("usuario") Usuario usuario);

    @Query("SELECT g FROM Grupo g WHERE g.estado = :estado " +
            "AND g.fechaHoraAlmuerzo > :fechaActual " +
            "ORDER BY g.fechaHoraAlmuerzo ASC")
    List<Grupo> findGruposDisponibles(
            @Param("estado") EstadoGrupo estado,
            @Param("fechaActual") LocalDateTime fechaActual
    );

    @Query("SELECT g FROM Grupo g WHERE g.creador.id = :usuarioId " +
            "OR :usuarioId IN (SELECT u.id FROM g.participantes u)")
    List<Grupo> findMisGrupos(@Param("usuarioId") Long usuarioId);
}