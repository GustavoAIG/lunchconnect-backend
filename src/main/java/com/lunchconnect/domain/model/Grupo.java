package com.lunchconnect.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "grupo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"participantes", "creador", "restaurante"})
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "El creador es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(max = 100)
    @Column(name = "nombre_grupo", nullable = false, length = 100)
    private String nombreGrupo;

    @Min(value = 2, message = "Mínimo 2 personas")
    @Column(name = "max_miembros", nullable = false)
    @Builder.Default
    private Integer maxMiembros = 5;

    @NotNull(message = "La fecha y hora del almuerzo es obligatoria")
    @Column(name = "fecha_hora_almuerzo", nullable = false)
    private LocalDateTime fechaHoraAlmuerzo;

    @NotNull(message = "El restaurante es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_elegido_id", nullable = false)
    private Restaurante restaurante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoGrupo estado = EstadoGrupo.ACTIVO;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "usuario_grupo",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    @Builder.Default
    private Set<Usuario> participantes = new HashSet<>();

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "chat_room_id", unique = true)
    private String chatRoomId;
    
    public enum EstadoGrupo {
        ACTIVO,
        LLENO,
        COMPLETADO,
        CANCELADO
    }


    public boolean estaLleno() {
        if (participantes == null) {
            participantes = new HashSet<>();
        }
        return participantes.size() >= maxMiembros;
    }

    public boolean puedeUnirse(Usuario usuario) {
        if (participantes == null) {
            participantes = new HashSet<>();
        }
        return !estaLleno() &&
                estado == EstadoGrupo.ACTIVO &&
                !participantes.contains(usuario);
    }

    public void agregarParticipante(Usuario usuario) {
        if (participantes == null) {
            participantes = new HashSet<>();
        }
        if (puedeUnirse(usuario)) {
            participantes.add(usuario);
            if (estaLleno()) {
                this.estado = EstadoGrupo.LLENO;
            }
        } else {
            throw new IllegalStateException("No se puede agregar al participante");
        }
    }

    public void eliminarParticipante(Usuario usuario) {
        if (participantes == null) {
            participantes = new HashSet<>();
            return;
        }
        participantes.remove(usuario);
        if (estado == EstadoGrupo.LLENO && !estaLleno()) {
            this.estado = EstadoGrupo.ACTIVO;
        }
    }

    public int espaciosDisponibles() {
        if (participantes == null) {
            participantes = new HashSet<>();
        }
        return maxMiembros - participantes.size();
    }

    public Set<Usuario> getParticipantes() {
        if (participantes == null) {
            participantes = new HashSet<>();
        }
        return participantes;
    }
}