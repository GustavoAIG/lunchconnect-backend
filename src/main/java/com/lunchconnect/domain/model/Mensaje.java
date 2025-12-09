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
    private Long id;

    // 💡 El ID del grupo al que pertenece el mensaje (Foreign Key)
    // Usamos el ID del grupo (Long) para mapear el chatRoomId de tu modelo Grupo
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    // 💡 El usuario que envió el mensaje (Foreign Key)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    @Lob // Para permitir mensajes más largos
    @Column(nullable = false)
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