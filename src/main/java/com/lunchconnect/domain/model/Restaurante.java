package com.lunchconnect.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "restaurante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"grupos"})
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_restaurante")
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String direccion;

    @NotBlank(message = "El distrito es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String distrito;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String categoria;

    @DecimalMin(value = "0.0", message = "La calificación mínima es 0.0")
    @DecimalMax(value = "5.0", message = "La calificación máxima es 5.0")
    @Column(name = "calificacion_promedio", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal calificacionPromedio = BigDecimal.ZERO;

    @Size(max = 255)
    @Column(name = "url_imagen", length = 255)
    private String urlImagen;

    @Column(name = "capacidad_maxima", nullable = false)
    private Integer capacidadMaxima;

    @Size(max = 150)
    @Column(name = "contacto_gerente", length = 150)
    private String contactoGerente;

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Grupo> grupos = new HashSet<>();
}