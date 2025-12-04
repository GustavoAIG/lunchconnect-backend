package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.CrearResenaRequest;
import com.lunchconnect.application.dto.ResenaDTO;
import com.lunchconnect.application.mapper.ResenaMapper;
import com.lunchconnect.domain.model.Resena;
import com.lunchconnect.domain.model.Restaurante;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.ResenaRepository;
import com.lunchconnect.domain.repository.RestauranteRepository;
import com.lunchconnect.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final ResenaMapper resenaMapper;

    public ResenaDTO crearResena(Long restauranteId, CrearResenaRequest request, Long usuarioId) {
        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar que el restaurante existe
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));

        // Validar que el usuario no haya reseñado ya este restaurante
        if (resenaRepository.existsByUsuarioIdAndRestauranteId(usuarioId, restauranteId)) {
            throw new RuntimeException("Ya has reseñado este restaurante");
        }

        // Crear la reseña
        Resena resena = Resena.builder()
                .usuario(usuario)
                .restaurante(restaurante)
                .textoResena(request.getTextoResena())
                .calificacion(request.getCalificacion())
                .build();

        Resena guardada = resenaRepository.save(resena);

        // Actualizar la calificación promedio del restaurante
        actualizarCalificacionPromedio(restauranteId);

        return resenaMapper.toDTO(guardada);
    }

    public List<ResenaDTO> obtenerResenasPorRestaurante(Long restauranteId) {
        if (!restauranteRepository.existsById(restauranteId)) {
            throw new RuntimeException("Restaurante no encontrado");
        }

        return resenaRepository.findByRestauranteIdOrderByFechaCreacionDesc(restauranteId)
                .stream()
                .map(resenaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ResenaDTO> obtenerResenasPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return resenaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(resenaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ResenaDTO actualizarResena(Long resenaId, CrearResenaRequest request, Long usuarioId) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        // Validar que el usuario es el dueño de la reseña
        if (!resena.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para editar esta reseña");
        }

        resena.setTextoResena(request.getTextoResena());
        resena.setCalificacion(request.getCalificacion());

        Resena actualizada = resenaRepository.save(resena);

        // Actualizar la calificación promedio del restaurante
        actualizarCalificacionPromedio(resena.getRestaurante().getId());

        return resenaMapper.toDTO(actualizada);
    }

    public void eliminarResena(Long resenaId, Long usuarioId) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        // Validar que el usuario es el dueño de la reseña
        if (!resena.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta reseña");
        }

        Long restauranteId = resena.getRestaurante().getId();
        resenaRepository.delete(resena);

        // Actualizar la calificación promedio del restaurante
        actualizarCalificacionPromedio(restauranteId);
    }

    private void actualizarCalificacionPromedio(Long restauranteId) {
        Double promedio = resenaRepository.calcularPromedioCalificacion(restauranteId);

        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));

        if (promedio != null) {
            BigDecimal promedioRedondeado = BigDecimal.valueOf(promedio)
                    .setScale(1, RoundingMode.HALF_UP);
            restaurante.setCalificacionPromedio(promedioRedondeado);
        } else {
            restaurante.setCalificacionPromedio(BigDecimal.ZERO);
        }

        restauranteRepository.save(restaurante);
    }
}