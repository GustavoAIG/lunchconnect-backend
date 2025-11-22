package com.lunchconnect.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El correo o nombre de usuario es obligatorio")
    private String correoOUsuario;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}