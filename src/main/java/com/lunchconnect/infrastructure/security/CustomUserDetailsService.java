package com.lunchconnect.infrastructure.security;

import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(usernameOrEmail)
                .or(() -> usuarioRepository.findByNombreUsuario(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con correo o username: " + usernameOrEmail));

        return User.builder()
                .username(usuario.getCorreoElectronico())
                .password(usuario.getContrasenaHash())
                .authorities(new ArrayList<>())
                .build();
    }

    public Usuario loadUserEntityByUsername(String usernameOrEmail) {
        return usuarioRepository.findByCorreoElectronico(usernameOrEmail)
                .or(() -> usuarioRepository.findByNombreUsuario(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + usernameOrEmail));
    }
}