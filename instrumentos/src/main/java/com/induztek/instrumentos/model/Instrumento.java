package com.induztek.instrumentos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "instrumentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instrumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInstrumento;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String marca;

    @Column(length = 100)
    private String modelo;

    @NotBlank
    @Column(name = "numero_serie", nullable = false, unique = true, length = 100)
    private String numeroSerie;

    @NotBlank
    @Column(name = "tipo_prueba", nullable = false, length = 30)
    private String tipoPrueba;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "bloqueado_por_vencimiento", nullable = false)
    @Builder.Default
    private boolean bloqueadoPorVencimiento = false;
}
