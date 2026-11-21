package com.induztek.auth.dto;

public record UsuarioDto(
        Long id,
        Long rolId,
        String correo,
        boolean activo,
        boolean debeCambiarClave
) {
}