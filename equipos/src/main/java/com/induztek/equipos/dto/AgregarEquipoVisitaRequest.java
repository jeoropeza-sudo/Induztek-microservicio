package com.induztek.equipos.dto;

import jakarta.validation.constraints.NotNull;

public record AgregarEquipoVisitaRequest(
        @NotNull(message = "El ID del equipo es obligatorio")
        Long idEquipo,
        Integer orden
) {
}
