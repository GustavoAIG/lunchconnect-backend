package com.lunchconnect.domain.repository;

import com.lunchconnect.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoElectronico(String correoElectronico);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByNombreUsuario(String nombreUsuario);

    /**
     * Busca usuarios por nombre de usuario o por los campos de nombre/apellido (que forman el nombre completo).
     */
    @Query("SELECT u FROM Usuario u WHERE " +
            // Busca por nombre de usuario (el tag)
            "LOWER(u.nombreUsuario) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            // Busca en el nombre completo (concatenando nombres y apellidos)
            "LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Usuario> searchByUsernameOrTag(@Param("term") String term);
}