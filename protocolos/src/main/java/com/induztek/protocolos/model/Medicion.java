package com.induztek.protocolos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mediciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedicion;

    @NotNull
    @Column(name = "id_protocolo", nullable = false)
    private Long idProtocolo;

    @Column(name = "id_instrumento")
    private Long idInstrumento;

    @NotBlank
    @Column(name = "tipo_prueba", nullable = false, length = 30)
    private String tipoPrueba;

    @Column(length = 100)
    private String nombre;

    @Column(name = "valor_1", length = 100)
    private String valor1;

    @Column(name = "valor_2", length = 100)
    private String valor2;

    @Column(name = "valor_3", length = 100)
    private String valor3;

    @Column(name = "valor_4", length = 100)
    private String valor4;

    @Column(length = 100)
    private String referencia;

    @Column(length = 20)
    private String unidad;

    @Column(length = 20)
    private String resultado;

    @Column(name = "instrumento_nombre", length = 150)
    private String instrumentoNombre;

    @Column(name = "instrumento_modelo", length = 100)
    private String instrumentoModelo;

    @Column(name = "instrumento_serie", length = 100)
    private String instrumentoSerie;

    @Column(name = "calibracion_vencimiento", length = 10)
    private String calibracionVencimiento;
}
