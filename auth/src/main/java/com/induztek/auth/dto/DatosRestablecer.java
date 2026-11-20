package com.induztek.auth.dto;

public record DatosRestablecer(
        String correo,
        String codigo,
        String contrasenaNueva
) {
}