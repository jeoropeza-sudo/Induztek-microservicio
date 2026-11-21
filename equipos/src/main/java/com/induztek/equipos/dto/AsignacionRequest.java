package com.induztek.equipos.dto;

import java.util.List;

public record AsignacionRequest(
        Long asignadoPor,
        List<Long> idsInstalacion
) {
}
