package com.lunchconnect.application.service;

import com.lunchconnect.domain.model.Grupo;
import com.lunchconnect.domain.repository.GrupoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledService {

    private final GrupoRepository grupoRepository;
    private final EmailService emailService;

    /**
     * Se ejecuta cada hora para actualizar grupos que ya pasaron
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora en punto
    @Transactional
    public void actualizarEstadosGrupos() {
        log.info("Ejecutando actualización de estados de grupos...");

        List<Grupo> gruposActivos = grupoRepository.findByEstado(Grupo.EstadoGrupo.ACTIVO);
        LocalDateTime ahora = LocalDateTime.now();

        int actualizados = 0;
        for (Grupo grupo : gruposActivos) {
            // Si el almuerzo ya pasó, cambiar a COMPLETADO
            if (grupo.getFechaHoraAlmuerzo().isBefore(ahora)) {
                grupo.setEstado(Grupo.EstadoGrupo.COMPLETADO);
                grupoRepository.save(grupo);
                actualizados++;
            }
        }

        log.info("Grupos actualizados a COMPLETADO: {}", actualizados);
    }

    /**
     * Envía recordatorios 24 horas antes del almuerzo
     * Se ejecuta cada hora
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void enviarRecordatorios() {
        log.info("Verificando grupos para enviar recordatorios...");

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime dentroDe24Horas = ahora.plusHours(24);

        List<Grupo> gruposProximos = grupoRepository.findByEstado(Grupo.EstadoGrupo.ACTIVO);

        int recordatoriosEnviados = 0;
        for (Grupo grupo : gruposProximos) {
            LocalDateTime fechaGrupo = grupo.getFechaHoraAlmuerzo();

            // Si el almuerzo es entre 23 y 25 horas en el futuro
            if (fechaGrupo.isAfter(ahora.plusHours(23)) &&
                    fechaGrupo.isBefore(dentroDe24Horas.plusHours(1))) {

                // Enviar recordatorio a todos los participantes
                grupo.getParticipantes().forEach(participante -> {
                    emailService.enviarEmailRecordatorio(participante, grupo);
                });

                recordatoriosEnviados += grupo.getParticipantes().size();
            }
        }

        log.info("Recordatorios enviados: {}", recordatoriosEnviados);
    }
}