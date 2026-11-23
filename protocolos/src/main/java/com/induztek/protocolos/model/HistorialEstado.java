package com.induztek.protocolos.model;

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
@Table(name = "historial_estados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_protocolo", nullable = false)
    private Long idProtocolo;

    @Column(name = "estado_anterior", length = 40)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 40)
    private String estadoNuevo;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "fecha_hora", nullable = false)
    private Long fechaHora;

    @Column(columnDefinition = "TEXT")
    private String motivo;
}
