package com.induztek.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario_avatars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUsuario {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Lob
    @Column(name = "contenido", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @Column(name = "tipo_contenido", nullable = false, length = 50)
    private String tipoContenido;
}
