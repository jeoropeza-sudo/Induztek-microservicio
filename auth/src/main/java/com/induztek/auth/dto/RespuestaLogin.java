package com.induztek.auth.dto;

public record RespuestaLogin(
        String token,
        Long usuarioId,
        Long rolId,
        boolean primerIngreso
) {
}