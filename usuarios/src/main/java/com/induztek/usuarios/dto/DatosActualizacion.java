package com.induztek.usuarios.dto;

public record DatosActualizacion(
        String nombre,
        String apellido,
        String rut,
        String telefono,
        Long empresaId,
        Long rolId
) {
}