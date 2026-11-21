package com.induztek.usuarios.dto;

public record UsuarioRespuesta(
        Long id,
        String nombre,
        String apellido,
        String rut,
        String correo,
        String telefono,
        Long empresaId,
        String empresaNombre,
        Long rolId,
        String rolNombre,
        boolean activo,
        boolean debeCambiarClave,
        String avatarUrl
) {
}