package com.lunchconnect.infrastructure.security;

import com.lunchconnect.infrastructure.security.JwtTokenProvider; // 💡 ASUMIMOS ESTA RUTA
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider tokenProvider;

    public WebSocketAuthInterceptor(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {

            // El token JWT se suele pasar como query parameter al conectar al WebSocket (ej: /ws?token=ABC.123...)
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null && tokenProvider.validateToken(token)) {

                // 1. Extraer el ID del usuario del token
                Long userId = tokenProvider.getUserIdFromToken(token);

                // 2. Crear un Principal con el ID del usuario como su 'name'
                Principal principal = () -> userId.toString();

                // 3. Adjuntar el Principal a la sesión de WebSocket
                attributes.put("principal", principal);

                // 4. Establecer el contexto de seguridad (necesario para headerAccessor.getUser() en el Controller)
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        principal, null, null);

                SecurityContextHolder.getContext().setAuthentication(auth);

                return true; // Autenticación exitosa, continuar
            }
        }

        // 5. Si el token no es válido o no está presente, denegar la conexión
        response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Limpiar el contexto de seguridad después del handshake (Buena práctica)
        SecurityContextHolder.clearContext();
    }
}