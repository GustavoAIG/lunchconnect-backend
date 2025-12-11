package com.lunchconnect.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            logger.debug("=== JWT Authentication Filter ===");
            logger.debug("URI: {}", request.getRequestURI());
            logger.debug("Method: {}", request.getMethod());
            logger.debug("JWT Token presente: {}", jwt != null);

            if (StringUtils.hasText(jwt)) {
                logger.debug("Token extraído (primeros 20 chars): {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);

                    logger.debug("Token válido para usuario: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.debug("UserDetails cargado: {}", userDetails.getUsername());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("SecurityContext actualizado con autenticación para: {}", username);
                } else {
                    logger.warn("Token JWT no es válido");
                }
            } else {
                logger.debug("No se encontró token JWT en el header Authorization");
            }
        } catch (Exception ex) {
            logger.error("No se pudo establecer la autenticación del usuario en el SecurityContext", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        logger.debug("Header Authorization completo: {}", bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            logger.debug("Token extraído exitosamente (longitud: {})", token.length());
            return token;
        }

        logger.debug("Header Authorization no tiene formato 'Bearer <token>'");
        return null;
    }
}
