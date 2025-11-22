package com.lunchconnect.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"grupos", "gruposCreados"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe ser un correo válido")
    @Size(max = 150)
    @Column(name = "correo_electronico", nullable = false, unique = true, length = 150)
    private String correoElectronico;

    @Size(max = 100)
    @Column(name = "nombre_usuario", unique = true, length = 100)
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 255)
    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @NotBlank(message = "El rubro profesional es obligatorio")
    @Size(max = 100)
    @Column(name = "rubro_profesional", nullable = false, length = 100)
    private String rubroProfesional;

    @NotBlank(message = "El título principal es obligatorio")
    @Size(max = 100)
    @Column(name = "titulo_principal", nullable = false, length = 100)
    private String tituloPrincipal;

    @Size(max = 255)
    @Column(length = 255)
    private String linkedin;

    @ManyToMany(mappedBy = "participantes")
    @Builder.Default
    private Set<Grupo> grupos = new HashSet<>();

    @OneToMany(mappedBy = "creador", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Grupo> gruposCreados = new HashSet<>();

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}