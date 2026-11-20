package com.induztek.protocolos.config;

import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.repository.ProtocoloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProtocoloRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                new Protocolo(null, 1L, 1L, 1792503000000L, "Aislamiento 5 kV DC", 12.9, 9.1, 6.2, 1.8, "CON_OBSERVACION", "Índice de polarización bajo 2,0: repetir la prueba en 3 meses.", "REGISTRADO_EN_TERRENO", null),
                new Protocolo(null, 2L, 1L, 1776367200000L, "Aislamiento 5 kV DC", 11.2, 8.7, 7.9, 2.6, "APROBADO", "", "FORMALIZADO", null),
                new Protocolo(null, 1L, 1L, 1776174300000L, "Aislamiento 5 kV DC", 13.1, 9.4, 7.8, 2.4, "APROBADO", "", "FORMALIZADO", null)
            ));
        }
    }
}