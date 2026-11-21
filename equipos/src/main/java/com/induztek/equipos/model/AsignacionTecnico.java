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
        name = "asignaciones_tecnico",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tecnico_instalacion",
                columnNames = {"id_tecnico", "id_instalacion"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAsignacion;

    @Column(name = "id_tecnico", nullable = false)
    private Long idTecnico;

    @Column(name = "id_instalacion", nullable = false)
    private Long idInstalacion;

    @Column(name = "asignado_por")
    private Long asignadoPor;
}
