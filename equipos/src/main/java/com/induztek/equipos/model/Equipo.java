package com.induztek.equipos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "equipos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipo;

    @NotBlank(message = "El código del equipo es obligatorio")
    @Column(unique = true, nullable = false)
    private String codigo;

    @NotBlank(message = "El tipo de equipo es obligatorio")
    private String tipo;

    private String instalacion;
    private String direccion;
    private Double latitud;
    private Double longitud;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long idCliente;
}