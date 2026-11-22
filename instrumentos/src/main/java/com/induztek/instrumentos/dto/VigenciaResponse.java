package com.induztek.instrumentos.dto;

import java.time.LocalDate;

public record VigenciaResponse(
        Long idInstrumento,
        boolean vigente,
        LocalDate fechaEvaluada,
        LocalDate fechaCalibracion,
        LocalDate fechaVencimiento,
        String certificado
) {
}
