package com.lunchconnect.application.service;

import com.lunchconnect.domain.model.Grupo;
import com.lunchconnect.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private static final String FROM_EMAIL = "noreply@lunchconnect.com";

    @Async
    public void enviarEmailBienvenida(Usuario usuario) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(usuario.getCorreoElectronico());
            message.setSubject("¡Bienvenido a LunchConnect! 🍽️");
            message.setText(String.format(
                    "Hola %s,\n\n" +
                            "¡Bienvenido a LunchConnect! 🎉\n\n" +
                            "Estamos emocionados de tenerte en nuestra comunidad de networking profesional.\n\n" +
                            "Con LunchConnect podrás:\n" +
                            "✓ Conectar con profesionales de diferentes industrias\n" +
                            "✓ Organizar almuerzos de networking\n" +
                            "✓ Descubrir nuevos restaurantes\n" +
                            "✓ Ampliar tu red profesional\n\n" +
                            "¡Empieza a crear o unirte a grupos de almuerzo ahora!\n\n" +
                            "Saludos,\n" +
                            "El equipo de LunchConnect",
                    usuario.getNombreCompleto()
            ));

            mailSender.send(message);
            log.info("Email de bienvenida enviado a: {}", usuario.getCorreoElectronico());
        } catch (Exception e) {
            log.error("Error enviando email de bienvenida: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailGrupoCreado(Usuario creador, Grupo grupo) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(creador.getCorreoElectronico());
            message.setSubject("✅ Grupo de almuerzo creado exitosamente");
            message.setText(String.format(
                    "Hola %s,\n\n" +
                            "¡Tu grupo de almuerzo ha sido creado exitosamente! 🎉\n\n" +
                            "📋 Detalles del grupo:\n" +
                            "• Nombre: %s\n" +
                            "• Fecha y hora: %s\n" +
                            "• Restaurante: %s\n" +
                            "• Ubicación: %s, %s\n" +
                            "• Capacidad: %d personas\n" +
                            "• Participantes actuales: 1 (tú)\n\n" +
                            "Otros profesionales pueden unirse a tu grupo. Te notificaremos cuando alguien se una.\n\n" +
                            "¡Nos vemos en el almuerzo!\n\n" +
                            "Saludos,\n" +
                            "El equipo de LunchConnect",
                    creador.getNombreCompleto(),
                    grupo.getNombreGrupo(),
                    grupo.getFechaHoraAlmuerzo().format(formatter),
                    grupo.getRestaurante().getNombre(),
                    grupo.getRestaurante().getDireccion(),
                    grupo.getRestaurante().getDistrito(),
                    grupo.getMaxMiembros()
            ));

            mailSender.send(message);
            log.info("Email de grupo creado enviado a: {}", creador.getCorreoElectronico());
        } catch (Exception e) {
            log.error("Error enviando email de grupo creado: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailNuevoParticipante(Usuario creador, Usuario nuevoParticipante, Grupo grupo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(creador.getCorreoElectronico());
            message.setSubject("🎉 Nuevo participante en tu grupo");
            message.setText(String.format(
                    "Hola %s,\n\n" +
                            "¡Buenas noticias! Un nuevo profesional se ha unido a tu grupo de almuerzo.\n\n" +
                            "👤 Nuevo participante:\n" +
                            "• Nombre: %s\n" +
                            "• Título: %s\n" +
                            "• Rubro: %s\n\n" +
                            "📋 Grupo: %s\n" +
                            "🍽️ Restaurante: %s\n" +
                            "📍 Ubicación: %s\n" +
                            "👥 Participantes: %d/%d\n\n" +
                            "¡Prepárate para hacer nuevas conexiones!\n\n" +
                            "Saludos,\n" +
                            "El equipo de LunchConnect",
                    creador.getNombreCompleto(),
                    nuevoParticipante.getNombreCompleto(),
                    nuevoParticipante.getTituloPrincipal(),
                    nuevoParticipante.getRubroProfesional(),
                    grupo.getNombreGrupo(),
                    grupo.getRestaurante().getNombre(),
                    grupo.getRestaurante().getDireccion(),
                    grupo.getParticipantes().size(),
                    grupo.getMaxMiembros()
            ));

            mailSender.send(message);
            log.info("Email de nuevo participante enviado a: {}", creador.getCorreoElectronico());
        } catch (Exception e) {
            log.error("Error enviando email de nuevo participante: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailConfirmacionUnion(Usuario participante, Grupo grupo) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(participante.getCorreoElectronico());
            message.setSubject("✅ Te has unido al grupo de almuerzo");
            message.setText(String.format(
                    "Hola %s,\n\n" +
                            "¡Te has unido exitosamente al grupo de almuerzo! 🎉\n\n" +
                            "📋 Detalles del almuerzo:\n" +
                            "• Grupo: %s\n" +
                            "• Fecha y hora: %s\n" +
                            "• Restaurante: %s\n" +
                            "• Dirección: %s, %s\n" +
                            "• Organizador: %s (%s)\n" +
                            "• Participantes: %d/%d\n\n" +
                            "💡 Consejos para el almuerzo:\n" +
                            "• Llega puntual\n" +
                            "• Prepara tu presentación (30 segundos)\n" +
                            "• Lleva tarjetas de presentación si tienes\n" +
                            "• ¡Sé auténtico y disfruta la conversación!\n\n" +
                            "Te enviaremos un recordatorio un día antes.\n\n" +
                            "¡Nos vemos pronto!\n\n" +
                            "Saludos,\n" +
                            "El equipo de LunchConnect",
                    participante.getNombreCompleto(),
                    grupo.getNombreGrupo(),
                    grupo.getFechaHoraAlmuerzo().format(formatter),
                    grupo.getRestaurante().getNombre(),
                    grupo.getRestaurante().getDireccion(),
                    grupo.getRestaurante().getDistrito(),
                    grupo.getCreador().getNombreCompleto(),
                    grupo.getCreador().getTituloPrincipal(),
                    grupo.getParticipantes().size(),
                    grupo.getMaxMiembros()
            ));

            mailSender.send(message);
            log.info("Email de confirmación de unión enviado a: {}", participante.getCorreoElectronico());
        } catch (Exception e) {
            log.error("Error enviando email de confirmación: {}", e.getMessage());
        }
    }

    @Async
    public void enviarEmailRecordatorio(Usuario usuario, Grupo grupo) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(usuario.getCorreoElectronico());
            message.setSubject("⏰ Recordatorio: Almuerzo de networking mañana");
            message.setText(String.format(
                    "Hola %s,\n\n" +
                            "Te recordamos que mañana tienes un almuerzo de networking. 📅\n\n" +
                            "📋 Detalles:\n" +
                            "• Grupo: %s\n" +
                            "• Fecha y hora: %s\n" +
                            "• Restaurante: %s\n" +
                            "• Dirección: %s, %s\n" +
                            "• Participantes: %d personas\n\n" +
                            "🎯 No olvides:\n" +
                            "✓ Confirmar tu asistencia\n" +
                            "✓ Revisar el perfil de los otros participantes\n" +
                            "✓ Preparar preguntas interesantes\n" +
                            "✓ Llevar tarjetas de presentación\n\n" +
                            "¡Nos vemos mañana!\n\n" +
                            "Saludos,\n" +
                            "El equipo de LunchConnect",
                    usuario.getNombreCompleto(),
                    grupo.getNombreGrupo(),
                    grupo.getFechaHoraAlmuerzo().format(formatter),
                    grupo.getRestaurante().getNombre(),
                    grupo.getRestaurante().getDireccion(),
                    grupo.getRestaurante().getDistrito(),
                    grupo.getParticipantes().size()
            ));

            mailSender.send(message);
            log.info("Email de recordatorio enviado a: {}", usuario.getCorreoElectronico());
        } catch (Exception e) {
            log.error("Error enviando email de recordatorio: {}", e.getMessage());
        }
    }
}