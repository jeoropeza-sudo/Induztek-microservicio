package com.induztek.equipos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_cambios")
@Getter
@Setter
@NoArgsConstructor
public class HistorialCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_entidad", nullable = false, length = 30)
    private String tipoEntidad;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @Column(nullable = false, length = 30)
    private String accion;

    @Column(name = "datos_antes", columnDefinition = "TEXT")
    private String datosAntes;

    @Column(name = "datos_despues", columnDefinition = "TEXT")
    private String datosDespues;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
