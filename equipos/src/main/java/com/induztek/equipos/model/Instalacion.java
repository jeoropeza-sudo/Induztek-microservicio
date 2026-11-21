package com.induztek.equipos.model;

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
@Table(name = "instalaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInstalacion;

    @NotNull(message = "El ID de la empresa es obligatorio")
    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @NotNull(message = "El ID del usuario cliente es obligatorio")
    @Column(name = "id_usuario_cliente", nullable = false)
    private Long idUsuarioCliente;

    @NotBlank(message = "El nombre de la instalación es obligatorio")
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 200)
    private String direccion;

    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;
}
