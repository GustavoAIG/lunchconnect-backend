package com.lunchconnect.domain.model;

import com.lunchconnect.domain.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "solicitud_amistad"
        // *** IMPORTANTE: Se ha ELIMINADO la restricción única ***
        // El control de que no haya solicitudes duplicadas (A->B y B->A)
        // se maneja ahora completamente en el AmigosService mediante las consultas JPQL
        // que verifican si ya existe una solicitud PENDIENTE o ACEPTADA en CUALQUIER dirección.
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
    private Long id; // CORREGIDO: Se usa 'id' en lugar de 'idSolicitud' para cumplir con la convención de JPA

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
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @CreationTimestamp
    @Column(name = "fecha_envio", updatable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
}