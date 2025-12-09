package com.lunchconnect.infrastructure.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            List<String> authorizationHeader = accessor.getNativeHeader("Authorization");

            if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                String token = authorizationHeader.get(0);

                if (token != null && token.startsWith("Bearer ")) {
                    String jwt = token.substring(7);

                    if (tokenProvider.validateToken(jwt)) {
                        // 💡 PASO CLAVE: EXTRAER CLAIMS (INCLUYENDO ROLES)
                        Claims claims = tokenProvider.getClaimsFromToken(jwt);

                        String username = claims.getSubject();
                        // El interceptor espera una lista de strings para los roles
                        List<String> roles = (List<String>) claims.get("roles");

                        List<? extends GrantedAuthority> authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);

                        accessor.setUser(authentication);

                        log.info("WebSocket: Usuario autenticado exitosamente: {}", username);
                        return message;
                    }
                }
            }
            log.warn("WebSocket: Conexión rechazada: Token JWT no válido o ausente.");
        }

        return message;
    }
}