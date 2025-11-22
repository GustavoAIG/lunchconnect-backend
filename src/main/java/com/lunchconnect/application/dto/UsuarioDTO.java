package com.lunchconnect.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String correoElectronico;
    private String nombreUsuario;
    private String rubroProfesional;
    private String tituloPrincipal;
    private String linkedin;
    private String nombreCompleto;
}