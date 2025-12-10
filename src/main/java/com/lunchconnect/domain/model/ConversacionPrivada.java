package com.lunchconnect.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(
        name = "conversacion_privada",
        // 💡 Mapea el índice UNIQUE uix_conversacion_par (LEAST/GREATEST)
        // El servicio se encarga de que usuarioA_id < usuarioB_id
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"usuario_a_id", "usuario_b_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Excluye las colecciones para evitar StackOverflowError en toString
@ToString(exclude = {"usuarioA", "usuarioB", "mensajes"})
public class ConversacionPrivada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    private Long idConversacion; // Mapea a id_conversacion

    // Usuario A (el ID más pequeño en la base de datos, por convención)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_a_id", nullable = false)
    private Usuario usuarioA; // Mapea a usuario_a_id

    // Usuario B (el ID más grande en la base de datos)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_b_id", nullable = false)
    private Usuario usuarioB; // Mapea a usuario_b_id

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion; // Mapea a fecha_creacion

    // Relación inversa (opcional, pero útil para obtener el historial)
    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MensajePrivado> mensajes;
}