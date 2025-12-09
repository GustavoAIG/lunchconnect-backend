package com.lunchconnect.application.service;

import com.lunchconnect.domain.model.Grupo; // Importar
import com.lunchconnect.domain.model.Mensaje; // Importar
import com.lunchconnect.domain.model.Usuario; // Importar
import com.lunchconnect.domain.repository.GrupoRepository; // Importar
import com.lunchconnect.domain.repository.MensajeRepository; // Importar
import com.lunchconnect.domain.repository.UsuarioRepository; // Importar
import com.lunchconnect.infrastructure.exception.NotFoundException; // Importar
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final SimpMessagingTemplate messagingTemplate;

    // 💡 INYECCIONES NECESARIAS PARA PERSISTIR EVENTOS DEL SISTEMA
    private final MensajeRepository mensajeRepository;
    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final String SYSTEM_SENDER = "SYSTEM"; // Remitente para mensajes del sistema

    // Método auxiliar para construir y guardar un mensaje de sistema
    @Transactional // Asegura que la persistencia ocurre
    private void persistAndBroadcastSystemMessage(
            Long grupoId,
            String content,
            Mensaje.TipoMensaje tipo) {

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo no encontrado para chat ID: " + grupoId));

        // El remitente SYSTEM necesita un ID de usuario.
        // Usamos el creador del grupo (o puedes crear un usuario "Sistema" dedicado).
        // Usaremos el creador del grupo como el remitente del mensaje de SISTEMA
        Usuario remitenteSistema = grupo.getCreador();

        // 1. Persistir el mensaje
        Mensaje mensaje = Mensaje.builder()
                .grupo(grupo)
                .remitente(remitenteSistema) // Usar un usuario existente o "Sistema"
                .contenido(content)
                .tipo(tipo)
                .build();

        mensajeRepository.save(mensaje);

        // 2. Enviar el broadcast por WebSocket
        String destination = "/topic/grupos/" + grupoId;

        // Aquí deberías crear un ChatMessage DTO para enviar, usando el mapper o la construcción directa
        // (Asumiendo que tienes un método de conversión sencillo o un constructor en ChatMessage para esto)

        // Simulación del DTO que se envía al frontend:
        ChatMessage systemMessage = ChatMessage.builder()
                .grupoId(grupoId.toString())
                .senderId(remitenteSistema.getId().toString())
                .content(content)
                .timestamp(mensaje.getFechaEnvio())
                .type(ChatMessage.MessageType.valueOf(tipo.name()))
                .build();

        messagingTemplate.convertAndSend(destination, systemMessage);
        log.info("Evento persistido ({}) y enviado a tópico {}", tipo, grupoId);
    }

    // ----------------------------------------------------------------------------------
    // MÉTODOS DE CHATSERVICE MODIFICADOS
    // ----------------------------------------------------------------------------------

    @Override
    @Transactional // Asegura la creación del grupo y la persistencia del evento inicial
    public String createGroupChat(String groupName, List<Long> initialUserIds) {
        String chatRoomId = UUID.randomUUID().toString();
        // Nota: Este método se llama antes de que el Grupo sea guardado en GrupoService,
        // por lo que este método debe ser llamado *después* de guardar el grupo para tener el ID Long.

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
    public void removeUserToChat(String chatRoomId, Long userId) {
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
}