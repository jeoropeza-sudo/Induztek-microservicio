package com.induztek.equipos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatosRecepcion(
        @NotBlank String nombre,
        @NotBlank String rut,
        @NotBlank String cargo,
        @NotNull byte[] firma
) {
}
