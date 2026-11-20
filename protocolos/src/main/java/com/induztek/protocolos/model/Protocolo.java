package com.induztek.protocolos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "protocolos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Protocolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProtocolo;

    @NotNull(message = "El ID del equipo es obligatorio")
    private Long idEquipo;

    @NotNull(message = "El ID del técnico es obligatorio")
    private Long idTecnico;

    @NotNull(message = "La fecha y hora son obligatorias")
    private Long fechaHora;

    @NotBlank(message = "El tipo de prueba es obligatorio")
    private String tipoPrueba;

    private Double atBt;
    private Double atTierra;
    private Double btTierra;
    private Double indicePolarizacion;

    @NotBlank(message = "El resultado es obligatorio")
    private String resultado; // APROBADO, RECHAZADO, OBSERVACION

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @NotBlank(message = "El estado es obligatorio")
    private String estado; // BORRADOR, FORMALIZADO

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] foto;
}