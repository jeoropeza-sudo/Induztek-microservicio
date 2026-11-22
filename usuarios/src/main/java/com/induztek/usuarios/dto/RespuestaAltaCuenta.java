package com.induztek.usuarios.dto;

public record RespuestaAltaCuenta(
        UsuarioRespuesta usuario,
        String contrasenaTemporal
) {
}
