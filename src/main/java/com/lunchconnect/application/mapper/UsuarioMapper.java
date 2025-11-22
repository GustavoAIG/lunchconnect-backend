package com.lunchconnect.application.mapper;

import com.lunchconnect.application.dto.UsuarioDTO;
import com.lunchconnect.domain.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;

        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correoElectronico(usuario.getCorreoElectronico())
                .nombreUsuario(usuario.getNombreUsuario())
                .rubroProfesional(usuario.getRubroProfesional())
                .tituloPrincipal(usuario.getTituloPrincipal())
                .linkedin(usuario.getLinkedin())
                .nombreCompleto(usuario.getNombreCompleto())
                .build();
    }
}