package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {

    List<Restaurante> findByDistrito(String distrito);

    List<Restaurante> findByCategoria(String categoria);

    @Query("SELECT r FROM Restaurante r WHERE " +
            "(:categoria IS NULL OR r.categoria = :categoria) AND " +
            "(:distrito IS NULL OR r.distrito = :distrito)")
    List<Restaurante> filtrarRestaurantes(
            @Param("categoria") String categoria,
            @Param("distrito") String distrito
    );
}