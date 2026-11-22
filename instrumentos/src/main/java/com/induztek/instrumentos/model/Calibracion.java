package com.induztek.instrumentos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "calibraciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calibracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCalibracion;

    @NotNull
    @Column(name = "id_instrumento", nullable = false)
    private Long idInstrumento;

    @NotNull
    @Column(name = "fecha_calibracion", nullable = false)
    private LocalDate fechaCalibracion;

    @NotNull
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(length = 150)
    private String laboratorio;

    @Column(length = 150)
    private String certificado;
}
