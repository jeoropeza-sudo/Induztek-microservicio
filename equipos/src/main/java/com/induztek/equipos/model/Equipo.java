package com.induztek.equipos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "equipos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_equipo_instalacion_tag",
                columnNames = {"id_instalacion", "tag"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipo;

    @NotBlank(message = "El código del equipo es obligatorio")
    @Column(nullable = false)
    private String codigo;

    @NotBlank(message = "El TAG del equipo es obligatorio")
    @Column(nullable = false, length = 50)
    private String tag;

    @NotBlank(message = "El tipo de equipo es obligatorio")
    @Column(length = 50)
    private String tipo;

    @Column(length = 50)
    private String clasificacion;

    @Column(length = 100)
    private String marca;

    @Column(length = 100)
    private String modelo;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "potencia_kva")
    private Double potenciaKva;

    @Column(length = 100)
    private String tensiones;

    @Column(name = "ano_fabricacion", length = 4)
    private String anoFabricacion;

    @Column(name = "posicion_tap", length = 20)
    private String posicionTap;

    @Column(name = "grupo_conexion", length = 30)
    private String grupoConexion;

    @Column(name = "resistencia_contacto_max_uohm")
    private Double resistenciaContactoMaxUohm;

    private String instalacion;

    @Column(name = "id_instalacion")
    private Long idInstalacion;
    private String direccion;
    private Double latitud;
    private Double longitud;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long idCliente;
}