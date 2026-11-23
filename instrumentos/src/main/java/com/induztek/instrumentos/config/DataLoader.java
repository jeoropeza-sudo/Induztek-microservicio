package com.induztek.instrumentos.config;

import com.induztek.instrumentos.model.Calibracion;
import com.induztek.instrumentos.model.Instrumento;
import com.induztek.instrumentos.repository.CalibracionRepository;
import com.induztek.instrumentos.repository.InstrumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final InstrumentoRepository instrumentoRepository;
    private final CalibracionRepository calibracionRepository;

    @Override
    public void run(String... args) {
        List<Instrumento> instrumentos;
        if (instrumentoRepository.count() == 0) {
            instrumentos = List.of(
                    instrumento("Megger MIT525", "MIT-0045821", "Megger", "MIT525", "AISLACION"),
                    instrumento("Megger MIT1025", "MIT-0071233", "Megger", "MIT1025", "AISLACION"),
                    instrumento("TTR Megger MTO330", "TTR-330-119", "Megger", "MTO330", "TTR"),
                    instrumento("Microhmímetro Megger DLRO10", "DLRO-10-071", "Megger", "DLRO10", "DEVANADOS"),
                    instrumento("Test set relé Omicron CMC", "CMC-356-004", "OMICRON", "CMC356", "RELE")
            );
            instrumentos = instrumentoRepository.saveAll(instrumentos);
        } else {
            List<String> seriesDemo = List.of(
                    "MIT-0045821", "MIT-0071233", "TTR-330-119", "DLRO-10-071", "CMC-356-004"
            );
            instrumentos = instrumentoRepository.findAll().stream()
                    .filter(instrumento -> seriesDemo.contains(instrumento.getNumeroSerie()))
                    .toList();
        }

        LocalDate hoy = LocalDate.now();
        for (Instrumento instrumento : instrumentos) {
            if (calibracionRepository
                    .findFirstByIdInstrumentoOrderByFechaVencimientoDesc(instrumento.getIdInstrumento())
                    .isEmpty()) {
                calibracionRepository.save(Calibracion.builder()
                        .idInstrumento(instrumento.getIdInstrumento())
                        .fechaCalibracion(hoy.minusMonths(6))
                        .fechaVencimiento(hoy.plusYears(1))
                        .laboratorio("Laboratorio Induztek")
                        .certificado("CERT-" + instrumento.getNumeroSerie())
                        .build());
            }
        }
    }

    private Instrumento instrumento(
            String nombre,
            String numeroSerie,
            String marca,
            String modelo,
            String tipoPrueba
    ) {
        return Instrumento.builder()
                .nombre(nombre)
                .numeroSerie(numeroSerie)
                .marca(marca)
                .modelo(modelo)
                .tipoPrueba(tipoPrueba)
                .activo(true)
                .build();
    }
}
