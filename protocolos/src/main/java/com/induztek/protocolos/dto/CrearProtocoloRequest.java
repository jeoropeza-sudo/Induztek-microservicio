package com.induztek.protocolos.dto;

import com.induztek.protocolos.model.Medicion;
import com.induztek.protocolos.model.Protocolo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CrearProtocoloRequest(
        @NotNull @Valid Protocolo protocolo,
        @Valid List<Medicion> mediciones
) {
}
