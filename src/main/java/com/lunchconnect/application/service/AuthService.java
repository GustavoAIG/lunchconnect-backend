package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.AuthResponse;
import com.lunchconnect.application.dto.LoginRequest;
import com.lunchconnect.application.dto.RegisterRequest;
import com.lunchconnect.application.dto.UsuarioDTO;
import com.lunchconnect.application.mapper.UsuarioMapper;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.UsuarioRepository;
import com.lunchconnect.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioMapper usuarioMapper;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByCorreoElectronico(request.getCorreoElectronico())) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        if (request.getNombreUsuario() != null &&
                !request.getNombreUsuario().isEmpty() &&
                usuarioRepository.existsByNombreUsuario(request.getNombreUsuario())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        Usuario usuario = Usuario.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .correoElectronico(request.getCorreoElectronico())
                .nombreUsuario(request.getNombreUsuario())
                .contrasenaHash(passwordEncoder.encode(request.getContrasena()))
                .rubroProfesional(request.getRubroProfesional())
                .tituloPrincipal(request.getTituloPrincipal())
                .linkedin(request.getLinkedin())
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();

        Usuario guardado = usuarioRepository.save(usuario);


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCorreoElectronico(),
                        request.getContrasena()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .usuario(usuarioMapper.toDTO(guardado))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCorreoOUsuario(),
                        request.getContrasena()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        Usuario usuario = usuarioRepository.findByCorreoElectronico(request.getCorreoOUsuario())
                .or(() -> usuarioRepository.findByNombreUsuario(request.getCorreoOUsuario()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return AuthResponse.builder()
                .token(token)
                .usuario(usuarioMapper.toDTO(usuario))
                .build();
    }

    public UsuarioDTO obtenerUsuarioActual(String correoElectronico) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(correoElectronico)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuarioMapper.toDTO(usuario);
    }

    // ------------------ NUEVO MÉTODO ------------------
    public String crearAdmin() {
        if (usuarioRepository.existsByCorreoElectronico("admin@lunchconnect.com")) {
            return "Admin ya existe";
        }

        Usuario admin = Usuario.builder()
                .nombres("Admin")
                .apellidos("Sistema")
                .correoElectronico("admin@lunchconnect.com")
                .nombreUsuario("admin")
                .contrasenaHash(passwordEncoder.encode("admin123"))
                .rubroProfesional("Administración")
                .tituloPrincipal("Administrador")
                .roles(new HashSet<>(Arrays.asList("USER", "ADMIN")))
                .build();

        usuarioRepository.save(admin);
        return "Admin creado exitosamente";
    }
}
