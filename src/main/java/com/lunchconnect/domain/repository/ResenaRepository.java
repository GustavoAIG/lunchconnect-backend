package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByRestauranteIdOrderByFechaCreacionDesc(Long restauranteId);

    List<Resena> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    Optional<Resena> findByUsuarioIdAndRestauranteId(Long usuarioId, Long restauranteId);

    @Query("SELECT AVG(r.calificacion) FROM Resena r WHERE r.restaurante.id = :restauranteId")
    Double calcularPromedioCalificacion(@Param("restauranteId") Long restauranteId);

    boolean existsByUsuarioIdAndRestauranteId(Long usuarioId, Long restauranteId);
}