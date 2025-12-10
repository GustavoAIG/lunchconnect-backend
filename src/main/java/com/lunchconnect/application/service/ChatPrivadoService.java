package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.MensajePrivadoDTO;
import com.lunchconnect.domain.model.ConversacionPrivada;
import com.lunchconnect.domain.model.MensajePrivado;
import com.lunchconnect.domain.model.Usuario;
import com.lunchconnect.domain.repository.ConversacionPrivadaRepository;
import com.lunchconnect.domain.repository.MensajePrivadoRepository;
import com.lunchconnect.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChatPrivadoService {

    private final ConversacionPrivadaRepository conversacionPrivadaRepository;
    private final MensajePrivadoRepository mensajePrivadoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Busca una conversación 1:1 existente o crea una nueva si no existe.
     * Esta lógica respeta la restricción UNIQUE del BD (LEAST/GREATEST).
     */
    private ConversacionPrivada getOrCreateConversacion(Long usuarioId1, Long usuarioId2) {

        return conversacionPrivadaRepository.findByUsuarios(usuarioId1, usuarioId2)
                .orElseGet(() -> {
                    // Si no existe, la creamos
                    Usuario u1 = usuarioRepository.findById(usuarioId1)
                            .orElseThrow(() -> new NoSuchElementException("Usuario 1 no encontrado."));
                    Usuario u2 = usuarioRepository.findById(usuarioId2)
                            .orElseThrow(() -> new NoSuchElementException("Usuario 2 no encontrado."));

                    ConversacionPrivada nuevaConversacion = new ConversacionPrivada();
                    // Ordenamos para respetar la restricción UNIQUE (usuarioA_id < usuarioB_id)
                    if (usuarioId1 < usuarioId2) {
                        nuevaConversacion.setUsuarioA(u1);
                        nuevaConversacion.setUsuarioB(u2);
                    } else {
                        nuevaConversacion.setUsuarioA(u2);
                        nuevaConversacion.setUsuarioB(u1);
                    }
                    nuevaConversacion.setFechaCreacion(LocalDateTime.now());
                    return conversacionPrivadaRepository.save(nuevaConversacion);
                });
    }

    /**
     * Guarda el mensaje enviado a través de WebSocket en la base de datos.
     */
    public MensajePrivado guardarMensaje(MensajePrivadoDTO mensajeDTO) {

        Long remitenteId = mensajeDTO.getRemitenteId();
        Long destinatarioId = mensajeDTO.getDestinatarioId();

        // Obtenemos o creamos la conversación
        ConversacionPrivada conversacion = getOrCreateConversacion(remitenteId, destinatarioId);

        Usuario remitente = usuarioRepository.findById(remitenteId)
                .orElseThrow(() -> new NoSuchElementException("Remitente no encontrado."));

        MensajePrivado mensaje = new MensajePrivado();
        mensaje.setConversacion(conversacion);
        mensaje.setRemitente(remitente);
        mensaje.setContenido(mensajeDTO.getContenido());
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setEstado("ENVIADO"); // Coincide con el ENUM/CHECK de la BD

        return mensajePrivadoRepository.save(mensaje);
    }

    /**
     * Obtiene el historial de mensajes de una conversación específica. (Para REST)
     */
    public List<MensajePrivado> obtenerHistorial(Long conversacionId) {
        // Verifica si la conversación existe antes de buscar mensajes si es necesario
        return mensajePrivadoRepository.findByConversacion_IdConversacionOrderByFechaEnvioAsc(conversacionId);
    }
}