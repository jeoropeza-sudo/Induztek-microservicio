package com.induztek.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String apellido;

    @Column(nullable = false, unique = true, length = 12)
    private String rut;

    @Column(unique = true, length = 100)
    private String correo;

    @Column(nullable = false, length = 12)
    private String telefono;

    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "debe_cambiar_clave", nullable = false)
    @Builder.Default
    private boolean debeCambiarClave = true;

    @Column(length = 100)
    private String avatar;
}