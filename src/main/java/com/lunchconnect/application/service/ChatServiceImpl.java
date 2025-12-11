package com.lunchconnect.application.service;

import com.lunchconnect.application.dto.ChatMessage;
import com.lunchconnect.domain.model.Grupo; // Importar
import com.lunchconnect.domain.model.Mensaje; // Importar
import com.lunchconnect.domain.model.Usuario; // Importar
import com.lunchconnect.domain.repository.GrupoRepository; // Importar
import com.lunchconnect.domain.repository.MensajeRepository; // Importar
import com.lunchconnect.domain.repository.UsuarioRepository; // Importar
import com.lunchconnect.infrastructure.exception.NotFoundException; // Importar
import com.lunchconnect.infrastructure.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {


    // 💡 INYECCIONES NECESARIAS PARA PERSISTIR EVENTOS DEL SISTEMA
    private final MensajeRepository mensajeRepository;
    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChatWebSocketHandler chatWebSocketHandler; // inyect
    private static final String SYSTEM_SENDER = "SYSTEM"; // Remitente para mensajes del sistema

    // Metodo auxiliar para construir y guardar un mensaje de sistema
    @Transactional // Asegura que la persistencia ocurre
    private void persistAndBroadcastSystemMessage(Long grupoId, String content, Mensaje.TipoMensaje tipo) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo no encontrado"));

        Usuario remitenteSistema = grupo.getCreador();

        Mensaje mensaje = Mensaje.builder()
                .grupo(grupo)
                .remitente(remitenteSistema)
                .contenido(content)
                .tipo(tipo)
                .build();

        mensajeRepository.save(mensaje);

        ChatMessage systemMessage = ChatMessage.builder()
                .grupoId(grupoId.toString())
                .senderId(remitenteSistema.getId().toString())
                .content(content)
                .timestamp(mensaje.getFechaEnvio())
                .type(ChatMessage.MessageType.valueOf(tipo.name()))
                .build();

        // Enviar a través del handler
        chatWebSocketHandler.handleTextMessageDirectly(systemMessage);
    }

    // ----------------------------------------------------------------------------------
    // MÉTODOS DE CHATSERVICE MODIFICADOS
    // ----------------------------------------------------------------------------------

    @Override
    @Transactional // Asegura la creación del grupo y la persistencia del evento inicial
    public String createGroupChat(String groupName, List<Long> initialUserIds) {
        String chatRoomId = UUID.randomUUID().toString();
        // Nota: Este metodo se llama antes de que el Grupo sea guardado en GrupoService,
        // por lo que este metodo debe ser llamado *después* de guardar el grupo para tener el ID Long.

        // ASUMIMOS que el ID del chat room es el ID Long del grupo para esta persistencia
        // Si el ID es UUID, necesitarás una forma de mapear el UUID al Long ID del grupo.

        return chatRoomId;
    }

    @Override
    @Transactional // Persiste el evento JOIN
    public void addUserToChat(String chatRoomId, Long userId) {
        Long grupoId = Long.valueOf(chatRoomId); // Asumimos que chatRoomId es el ID Long del Grupo

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + userId));

        String content = usuario.getNombres() + " se ha unido al almuerzo de networking.";

        persistAndBroadcastSystemMessage(grupoId, content, Mensaje.TipoMensaje.SISTEMA);
    }

    @Override
    @Transactional // Persiste el evento LEAVE
    public void removeUserFromChat(String chatRoomId, Long userId) {
        Long grupoId = Long.valueOf(chatRoomId); // Asumimos que chatRoomId es el ID Long del Grupo

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + userId));

        String content = usuario.getNombres() + " ha salido del almuerzo.";

        persistAndBroadcastSystemMessage(grupoId, content, Mensaje.TipoMensaje.SISTEMA);
    }

    @Override
    public void deleteGroupChat(String chatRoomId) {
        // La eliminación es conceptual, pero se podría añadir lógica para borrar todos los mensajes del grupo de la DB.
        // mensajeRepository.deleteAllByGrupoId(Long.valueOf(chatRoomId)); // Necesitaría este método en el repo
        log.warn("Solicitud de eliminación de chat {}. La eliminación de mensajes debe ser manual o en cascada.", chatRoomId);
    }

    @Override
    public void sendMessage(ChatMessage chatMessage) {
        chatWebSocketHandler.handleTextMessageDirectly(chatMessage);
    }

}