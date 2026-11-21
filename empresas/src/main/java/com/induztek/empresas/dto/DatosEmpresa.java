package com.induztek.empresas.dto;

public record DatosEmpresa(
        String nombre,
        String rut,
        String giro,
        String direccion,
        String contactoNombre,
        String contactoCorreo,
        String contactoTelefono
) {
}