package com.induztek.equipos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "visita_equipos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_visita_equipo",
                columnNames = {"id_visita", "id_equipo"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitaEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_visita", nullable = false)
    private Long idVisita;

    @Column(name = "id_equipo", nullable = false)
    private Long idEquipo;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 1;

    @Column(length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";
}
