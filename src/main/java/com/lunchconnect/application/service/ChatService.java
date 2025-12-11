package com.lunchconnect.application.service; // O en 'infrastructure'

import com.lunchconnect.application.dto.ChatMessage;

import java.util.List;

public interface ChatService {


    /**
     * Crea un canal de chat para un nuevo almuerzo de networking.
     * @param groupName El nombre del almuerzo.
     * @param initialUserIds Lista de IDs de los usuarios iniciales (normalmente el creador).
     * @return El ID de referencia del chat room (e.g., UUID, ID de la plataforma de chat).
     */
    String createGroupChat(String groupName, List<Long> initialUserIds);

    // Nuevo metodo para agregar miembros al chat cuando se unen al grupo
    void addUserToChat(String chatRoomId, Long userId);

    // Nuevo metodo para remover miembros del chat cuando abandonan el grupo
    void removeUserFromChat(String chatRoomId, Long userId);

    // Nuevo metodo para eliminar el chat cuando el grupo es eliminado
    void deleteGroupChat(String chatRoomId);

    void sendMessage(ChatMessage chatMessage);
}