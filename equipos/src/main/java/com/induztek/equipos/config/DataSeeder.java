package com.induztek.equipos.config;

import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.repository.EquipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EquipoRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                new Equipo(null, "TR-0142", "Transformador 500 kVA", "Planta Lampa · S/E-02", "Camino Los Maitenes 1200, Lampa", -33.2845, -70.8752, 2L),
                new Equipo(null, "TR-0098", "Transformador 300 kVA", "Planta Lampa · S/E-01", "Camino Los Maitenes 1200, Lampa", -33.2845, -70.8752, 2L),
                new Equipo(null, "TR-0215", "Transformador 1000 kVA", "Planta Quilicura · S/E-03", "Av. Américo Vespucio 1500, Quilicura", -33.3611, -70.7305, 2L),
                new Equipo(null, "TR-0330", "Transformador 750 kVA", "Planta Puerto Montt · S/E-01", "Ruta 5 Sur Km 1020, Puerto Montt", -41.4693, -72.9424, 3L)
            ));
        }
    }
}