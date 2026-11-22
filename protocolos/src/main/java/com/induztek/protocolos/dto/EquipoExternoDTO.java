package com.induztek.protocolos.dto;

import lombok.Data;

@Data
public class EquipoExternoDTO {
    private Long idEquipo;
    private String codigo;
    private String tipo;
    private String instalacion;
    private String tag;
    private Long idInstalacion;
    private Long idCliente;
}