package com.induztek.equipos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "visitas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVisita;

    @Column(name = "uuid_sync", nullable = false, unique = true, length = 36)
    private String uuidSync;

    @NotBlank
    @Column(name = "numero_ot", nullable = false, unique = true, length = 50)
    private String numeroOt;

    @NotNull
    @Column(name = "fecha_visita", nullable = false)
    private LocalDateTime fechaVisita;

    @NotNull
    @Column(name = "id_instalacion", nullable = false)
    private Long idInstalacion;

    @NotNull
    @Column(name = "id_tecnico", nullable = false)
    private Long idTecnico;

    @NotBlank
    @Column(name = "tipo_visita", nullable = false, length = 50)
    private String tipoVisita;

    @Column(name = "descripcion_mantenimiento", columnDefinition = "TEXT")
    private String descripcionMantenimiento;

    private Double temperaturaC;
    private Double humedadPct;

    @Column(name = "receptor_nombre", length = 100)
    private String receptorNombre;

    @Column(name = "receptor_rut", length = 20)
    private String receptorRut;

    @Column(name = "receptor_cargo", length = 100)
    private String receptorCargo;

    @Lob
    @Column(name = "firma_receptor", columnDefinition = "LONGBLOB")
    private byte[] firmaReceptor;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "EN_PREPARACION";

    @Column(name = "motivo_no_realizada", columnDefinition = "TEXT")
    private String motivoNoRealizada;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
