package com.lunchconnect.domain.model;

import com.lunchconnect.domain.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "solicitud_amistad",
        // Esta restricción asegura que solo haya una solicitud pendiente entre dos IDs
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"remitente_id", "destinatario_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"remitente", "destinatario"})
public class SolicitudAmistad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud; // Mapea a id_solicitud

    // Usuario que envía la solicitud (FK a usuario.id_usuario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    // Usuario que recibe la solicitud (FK a usuario.id_usuario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE; // Mapea a estado

    @CreationTimestamp
    @Column(name = "fecha_envio", updatable = false)
    private LocalDateTime fechaEnvio; // Mapea a fecha_envio

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta; // Mapea a fecha_respuesta
}