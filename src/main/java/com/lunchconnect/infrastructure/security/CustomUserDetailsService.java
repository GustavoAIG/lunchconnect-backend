package com.lunchconnect.infrastructure.security;

import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

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
                .authorities(getAuthorities(usuario))
                .build();
    }

    public Usuario loadUserEntityByUsername(String usernameOrEmail) {
        return usuarioRepository.findByCorreoElectronico(usernameOrEmail)
                .or(() -> usuarioRepository.findByNombreUsuario(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + usernameOrEmail));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        return usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol))
                .collect(Collectors.toList());
    }
}