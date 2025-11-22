package com.lunchconnect.presentation.controller;

import com.lunchconnect.application.dto.RestauranteDTO;
import com.lunchconnect.application.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class RestauranteController {

    private final RestauranteService restauranteService;

    @GetMapping
    public ResponseEntity<List<RestauranteDTO>> obtenerTodos() {
        return ResponseEntity.ok(restauranteService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestauranteDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(restauranteService.obtenerPorId(id));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<RestauranteDTO>> filtrar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String distrito) {
        return ResponseEntity.ok(restauranteService.filtrarRestaurantes(categoria, distrito));
    }

    @PostMapping
    public ResponseEntity<RestauranteDTO> crear(@RequestBody RestauranteDTO restauranteDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restauranteService.crear(restauranteDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        restauranteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}