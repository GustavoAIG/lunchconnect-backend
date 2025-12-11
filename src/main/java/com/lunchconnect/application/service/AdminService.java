package com.lunchconnect.application.service;

import com.lunchconnect.application.service.dto.UsuarioAdminDTO;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;

    // -------------------------------------------------------------------------
    // Mapeo
    // -------------------------------------------------------------------------

    private UsuarioAdminDTO mapToAdminDTO(Usuario usuario) {
        return new UsuarioAdminDTO(
                usuario.getId(),
                usuario.getNombreCompleto(), // Método existente en tu Usuario.java
                usuario.getNombreUsuario(),
                usuario.getCorreoElectronico(),
                usuario.getRubroProfesional(),
                usuario.getTituloPrincipal(),
                usuario.getRoles(),
                usuario.getFechaCreacion()
        );
    }

    private Usuario findUsuarioById(Long userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + userId));
    }

    // -------------------------------------------------------------------------
    // Funcionalidades de Lectura
    // -------------------------------------------------------------------------

    /**
     * Obtiene la lista completa de todos los usuarios registrados.
     * @return Lista de UsuarioAdminDTO.
     */
    @Transactional(readOnly = true)
    public List<UsuarioAdminDTO> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToAdminDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los detalles de un solo usuario para la vista de edición.
     * @param userId ID del usuario.
     * @return UsuarioAdminDTO.
     */
    @Transactional(readOnly = true)
    public UsuarioAdminDTO getUsuarioDetails(Long userId) {
        Usuario usuario = findUsuarioById(userId);
        return mapToAdminDTO(usuario);
    }

    // -------------------------------------------------------------------------
    // Funcionalidades de Escritura (Ejemplos)
    // -------------------------------------------------------------------------

    /**
     * Actualiza los roles de un usuario específico.
     * @param userId ID del usuario a modificar.
     * @param newRoles El nuevo conjunto de roles.
     */
    @Transactional
    public void updateUsuarioRoles(Long userId, Set<String> newRoles) {
        Usuario usuario = findUsuarioById(userId);
        usuario.setRoles(newRoles);
        usuarioRepository.save(usuario);
        // Nota: Si usas Spring Security, quizás debas actualizar la sesión o el token.
    }

    /**
     * Elimina un usuario del sistema (Ejemplo de operación peligrosa).
     * @param userId ID del usuario a eliminar.
     */
    @Transactional
    public void deleteUsuario(Long userId) {
        if (usuarioRepository.existsById(userId)) {
            // Considera limpiar dependencias (chats, grupos, solicitudes) antes de eliminar.
            usuarioRepository.deleteById(userId);
        } else {
            throw new NoSuchElementException("Usuario no encontrado para eliminar con ID: " + userId);
        }
    }


}