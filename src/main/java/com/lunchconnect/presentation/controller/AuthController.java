package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.AuthResponse;
import com.lunchconnect.application.dto.LoginRequest;
import com.lunchconnect.application.dto.RegisterRequest;
import com.lunchconnect.application.dto.UsuarioDTO;
import com.lunchconnect.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> obtenerUsuarioActual(Authentication authentication) {
        String correo = authentication.getName();
        UsuarioDTO usuario = authService.obtenerUsuarioActual(correo);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin() {
        // Lógica movida a AuthService
        String resultado = authService.crearAdmin();
        return ResponseEntity.ok(resultado);
    }
}
