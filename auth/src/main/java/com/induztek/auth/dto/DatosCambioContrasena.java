package com.induztek.auth.dto;

public record DatosCambioContrasena(
        String correo,
        String contrasenaActual,
        String contrasenaNueva
) {
}