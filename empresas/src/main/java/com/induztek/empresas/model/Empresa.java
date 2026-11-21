package com.induztek.empresas.model;

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
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(unique = true, length = 20)
    private String rut;

    @Column(length = 100)
    private String giro;

    @Column(nullable = false, length = 100)
    private String direccion;

    @Column(name = "contacto_nombre", length = 100)
    private String contactoNombre;

    @Column(name = "contacto_correo", length = 100)
    private String contactoCorreo;

    @Column(name = "contacto_telefono", length = 20)
    private String contactoTelefono;
}