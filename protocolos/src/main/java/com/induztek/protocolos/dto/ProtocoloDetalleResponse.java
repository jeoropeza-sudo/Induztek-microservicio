package com.induztek.protocolos.dto;

import com.induztek.protocolos.model.Protocolo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtocoloDetalleResponse {
    private Protocolo protocolo;
    private String codigoEquipo;
    private String tipoEquipo;
    private String instalacion;
    private String nombreTecnico;
}