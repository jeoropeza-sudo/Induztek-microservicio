package com.induztek.protocolos.config;

import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.repository.ProtocoloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProtocoloRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                protocolo(1L, 1L, "Aislamiento 5 kV DC", "CON_OBSERVACION",
                        "Índice de polarización bajo 2,0: repetir la prueba en 3 meses."),
                protocolo(2L, 1L, "Aislamiento 5 kV DC", "APROBADO", ""),
                protocolo(1L, 1L, "Aislamiento 5 kV DC", "APROBADO", "")
            ));
        }
    }

    private Protocolo protocolo(
            Long idEquipo,
            Long idTecnico,
            String tipoPrueba,
            String resultado,
            String observaciones
    ) {
        Protocolo protocolo = new Protocolo();
        protocolo.setIdEquipo(idEquipo);
        protocolo.setIdTecnico(idTecnico);
        protocolo.setFechaHora(System.currentTimeMillis());
        protocolo.setTipoPrueba(tipoPrueba);
        protocolo.setResultado(resultado);
        protocolo.setResultadoGlobal(resultado);
        protocolo.setObservaciones(observaciones);
        protocolo.setEstado("REGISTRADO_EN_TERRENO");
        protocolo.setAtBt(12.0);
        protocolo.setAtTierra(9.0);
        protocolo.setBtTierra(7.0);
        protocolo.setIndicePolarizacion(2.0);
        return protocolo;
    }
}