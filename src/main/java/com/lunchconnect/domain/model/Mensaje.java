package com.lunchconnect.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje") // Coherencia con la base de datos
    private Long idMensaje;      // Cambio de 'id' a 'idMensaje' (Opcional, pero recomendado)

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    // 💡 CAMBIO: Usar columnDefinition="TEXT" para mapear el tipo corregido en la BD
    @Column(name = "contenido", columnDefinition = "TEXT", nullable = false)
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TipoMensaje tipo = TipoMensaje.CHAT;

    @CreationTimestamp
    @Column(name = "fecha_envio", updatable = false)
    private LocalDateTime fechaEnvio;

    public enum TipoMensaje {
        CHAT,        // Mensaje normal de usuario
        SISTEMA,     // Notificaciones del sistema (ej. Usuario se unió)
        JOIN,        // Mensaje de usuario al unirse
        LEAVE        // Mensaje de usuario al salir
    }
}