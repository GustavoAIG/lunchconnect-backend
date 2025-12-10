package com.lunchconnect.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MensajePrivadoDTO {

    // El ID del remitente se obtiene del JWT en el backend, pero se usa para consistencia.
    private Long remitenteId;

    // El ID del usuario al que se envía el mensaje (Destino del WebSocket)
    private Long destinatarioId;

    private String contenido;

    // Propiedades de la respuesta (opcional, si el DTO también se usa para la respuesta)
    private Long conversacionId;
    private LocalDateTime fechaEnvio;
}