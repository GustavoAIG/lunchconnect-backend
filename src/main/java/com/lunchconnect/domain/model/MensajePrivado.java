package com.lunchconnect.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje_privado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"conversacion", "remitente"})
public class MensajePrivado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long idMensaje; // Mapea a id_mensaje

    // 💡 Clave Foránea a la conversación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private ConversacionPrivada conversacion; // Mapea a conversacion_id [cite: 19]

    // 💡 Clave Foránea al remitente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente; // Mapea a remitente_id [cite: 19]

    // Mapea al tipo TEXT de PostgreSQL
    @Column(name = "contenido", columnDefinition = "TEXT", nullable = false)
    private String contenido; // Mapea a contenido [cite: 19]

    @CreationTimestamp
    @Column(name = "fecha_envio", updatable = false)
    private LocalDateTime fechaEnvio; // Mapea a fecha_envio [cite: 19]

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    @Builder.Default
    private EstadoMensajePrivado estado = EstadoMensajePrivado.ENVIADO; // Mapea a estado [cite: 19]

    public enum EstadoMensajePrivado {
        ENVIADO, // Coincide con el check constraint del BD [cite: 19]
        LEIDO    // Coincide con el check constraint del BD [cite: 19]
    }
}