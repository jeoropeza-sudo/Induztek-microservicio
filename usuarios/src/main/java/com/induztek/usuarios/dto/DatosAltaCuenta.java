package com.induztek.usuarios.dto;

public record DatosAltaCuenta(
        Long actorId,
        String nombre,
        String apellido,
        String rut,
        String correo,
        String telefono,
        Long empresaId,
        Long rolId
) {
}
