package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.*;
import com.lunchconnect.application.mapper.GrupoMapper;
import com.lunchconnect.application.mapper.UsuarioMapper;
import com.lunchconnect.domain.model.Grupo;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.GrupoRepository;
import com.lunchconnect.domain.repository.RestauranteRepository;
import com.lunchconnect.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final RestauranteRepository restauranteRepository;
    private final UsuarioMapper usuarioMapper;
    private final GrupoMapper grupoMapper;

    public EstadisticasDTO obtenerEstadisticas() {
        Long totalUsuarios = usuarioRepository.count();
        Long totalGrupos = grupoRepository.count();
        Long totalRestaurantes = restauranteRepository.count();

        Long gruposActivos = Long.valueOf(grupoRepository.findByEstado(Grupo.EstadoGrupo.ACTIVO).size());
        Long gruposCompletados = Long.valueOf(grupoRepository.findByEstado(Grupo.EstadoGrupo.COMPLETADO).size());

        // Usuarios con al menos un grupo
        Long usuariosActivos = usuarioRepository.findAll().stream()
                .filter(u -> !u.getGrupos().isEmpty() || !u.getGruposCreados().isEmpty())
                .count();

        // Promedio de participantes por grupo
        List<Grupo> grupos = grupoRepository.findAll();
        Double promedioParticipantes = grupos.isEmpty() ? 0.0 :
                grupos.stream()
                        .mapToInt(g -> g.getParticipantes().size())
                        .average()
                        .orElse(0.0);

        // Restaurante más popular (con más grupos)
        String restauranteMasPopular = restauranteRepository.findAll().stream()
                .max((r1, r2) -> Integer.compare(r1.getGrupos().size(), r2.getGrupos().size()))
                .map(r -> r.getNombre())
                .orElse("N/A");

        return EstadisticasDTO.builder()
                .totalUsuarios(totalUsuarios)
                .totalGrupos(totalGrupos)
                .totalRestaurantes(totalRestaurantes)
                .gruposActivos(gruposActivos)
                .gruposCompletados(gruposCompletados)
                .usuariosActivos(usuariosActivos)
                .promedioParticipantesPorGrupo(promedioParticipantes)
                .restauranteMasPopular(restauranteMasPopular)
                .build();
    }

    public List<UsuarioDTO> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GrupoDTO> obtenerTodosLosGrupos() {
        return grupoRepository.findAll().stream()
                .map(grupoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void eliminarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar que no sea admin
        if (usuario.getRoles().contains("ADMIN")) {
            throw new RuntimeException("No se puede eliminar un administrador");
        }

        usuarioRepository.delete(usuario);
    }

    public void asignarRolAdmin(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.getRoles().add("ADMIN");
        usuarioRepository.save(usuario);
    }

    public void removerRolAdmin(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.getRoles().remove("ADMIN");
        usuarioRepository.save(usuario);
    }

    public List<RestaurantePopularDTO> obtenerRestaurantesMasPopulares(int limite) {
        return restauranteRepository.findAll().stream()
                .map(r -> {
                    long cantidadGrupos = r.getGrupos().size();
                    long cantidadParticipantes = r.getGrupos().stream()
                            .mapToLong(g -> g.getParticipantes().size())
                            .sum();

                    return RestaurantePopularDTO.builder()
                            .restauranteId(r.getId())
                            .nombre(r.getNombre())
                            .distrito(r.getDistrito())
                            .categoria(r.getCategoria())
                            .cantidadGrupos(cantidadGrupos)
                            .cantidadParticipantes(cantidadParticipantes)
                            .build();
                })
                .sorted((r1, r2) -> Long.compare(r2.getCantidadGrupos(), r1.getCantidadGrupos()))
                .limit(limite)
                .collect(Collectors.toList());
    }

    public List<MetricasMensualesDTO> obtenerMetricasMensuales(int meses) {
        LocalDateTime fechaInicio = LocalDateTime.now().minusMonths(meses);
        List<Grupo> grupos = grupoRepository.findAll().stream()
                .filter(g -> g.getFechaCreacion().isAfter(fechaInicio))
                .collect(Collectors.toList());

        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> u.getFechaCreacion().isAfter(fechaInicio))
                .collect(Collectors.toList());

        // Agrupar por mes
        Map<String, Long> gruposPorMes = grupos.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ));

        Map<String, Long> usuariosPorMes = usuarios.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ));

        Map<String, Long> participantesPorMes = grupos.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.summingLong(g -> g.getParticipantes().size())
                ));

        // Crear lista de métricas
        return gruposPorMes.keySet().stream()
                .sorted()
                .map(mes -> MetricasMensualesDTO.builder()
                        .mes(mes)
                        .gruposCreados(gruposPorMes.getOrDefault(mes, 0L))
                        .usuariosRegistrados(usuariosPorMes.getOrDefault(mes, 0L))
                        .participantesTotales(participantesPorMes.getOrDefault(mes, 0L))
                        .build())
                .collect(Collectors.toList());
    }

    public Map<String, Object> obtenerDashboard() {
        EstadisticasDTO stats = obtenerEstadisticas();
        List<RestaurantePopularDTO> topRestaurantes = obtenerRestaurantesMasPopulares(5);
        List<MetricasMensualesDTO> metricas = obtenerMetricasMensuales(6);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("estadisticas", stats);
        dashboard.put("restaurantesPopulares", topRestaurantes);
        dashboard.put("metricasMensuales", metricas);

        return dashboard;
    }
}