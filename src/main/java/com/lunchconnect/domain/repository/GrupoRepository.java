package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.Grupo;
import com.lunchconnect.domain.model.Grupo.EstadoGrupo;
import com.lunchconnect.domain.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Grupo> findByEstado(EstadoGrupo estado, Pageable pageable);

    @Query("SELECT g FROM Grupo g WHERE g.estado = :estado " +
            "AND g.fechaHoraAlmuerzo > :fechaActual " +
            "ORDER BY g.fechaHoraAlmuerzo ASC")
    Page<Grupo> findGruposDisponibles(
            @Param("estado") EstadoGrupo estado,
            @Param("fechaActual") LocalDateTime fechaActual,
            Pageable pageable
    );

    @Query("SELECT g FROM Grupo g WHERE " +
            "(:nombreGrupo IS NULL OR LOWER(g.nombreGrupo) LIKE LOWER(CONCAT('%', :nombreGrupo, '%'))) AND " +
            "(:distrito IS NULL OR g.restaurante.distrito = :distrito) AND " +
            "(:categoria IS NULL OR g.restaurante.categoria = :categoria) AND " +
            "(:fechaInicio IS NULL OR g.fechaHoraAlmuerzo >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR g.fechaHoraAlmuerzo <= :fechaFin) AND " +
            "g.estado = 'ACTIVO'")
    List<Grupo> buscarGrupos(
            @Param("nombreGrupo") String nombreGrupo,
            @Param("distrito") String distrito,
            @Param("categoria") String categoria,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}