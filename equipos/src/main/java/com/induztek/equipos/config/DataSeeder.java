package com.induztek.equipos.config;

import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.repository.EquipoRepository;
import com.induztek.equipos.model.Instalacion;
import com.induztek.equipos.repository.InstalacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EquipoRepository repository;
    private final InstalacionRepository instalacionRepository;

    @Override
    public void run(String... args) {
        if (instalacionRepository.count() == 0) {
            instalacionRepository.saveAll(List.of(
                    Instalacion.builder().idEmpresa(1L).idUsuarioCliente(2L).nombre("Planta Lampa").direccion("Camino Los Maitenes 1200, Lampa").activa(true).build(),
                    Instalacion.builder().idEmpresa(2L).idUsuarioCliente(3L).nombre("Planta Quilicura").direccion("Av. Américo Vespucio 1500, Quilicura").activa(true).build(),
                    Instalacion.builder().idEmpresa(3L).idUsuarioCliente(4L).nombre("Planta Puerto Montt").direccion("Ruta 5 Sur Km 1020, Puerto Montt").activa(true).build()
            ));
        }
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    equipo("TR-0142", "Transformador", "Rhona", "500 kVA", "RH-0142", 500.0,
                            "12 / 0.4 kV", "2012", "3", "Dyn11", 1L, 2L,
                            "Planta Lampa · S/E-02", "Camino Los Maitenes 1200, Lampa", -33.2845, -70.8752),
                    equipo("TR-0098", "Transformador", "Tusan", "300 kVA", "TS-0098", 300.0,
                            "12 / 0.4 kV", "2017", "2", "Dyn11", 1L, 2L,
                            "Planta Lampa · S/E-01", "Camino Los Maitenes 1200, Lampa", -33.2845, -70.8752),
                    equipo("TR-0215", "Transformador", "ABB", "1000 kVA", "ABB-0215", 1000.0,
                            "23 / 0.4 kV", "2009", "1", "Dyn1", 2L, 2L,
                            "Planta Quilicura · S/E-03", "Av. Américo Vespucio 1500, Quilicura", -33.3611, -70.7305),
                    equipo("TR-0330", "Transformador", "Schneider", "750 kVA", "SE-0330", 750.0,
                            "23 / 6.6 kV", "2011", "4", "Dyn11", 3L, 3L,
                            "Planta Puerto Montt · S/E-01", "Ruta 5 Sur Km 1020, Puerto Montt", -41.4693, -72.9424)
            ));
        }
    }

    private Equipo equipo(
            String tag,
            String tipo,
            String marca,
            String modelo,
            String numeroSerie,
            Double potenciaKva,
            String tensiones,
            String ano,
            String tap,
            String grupo,
            Long idInstalacion,
            Long idCliente,
            String instalacion,
            String direccion,
            Double latitud,
            Double longitud
    ) {
        Equipo equipo = new Equipo();
        equipo.setCodigo(tag);
        equipo.setTag(tag);
        equipo.setTipo(tipo);
        equipo.setClasificacion("PRIMARIO");
        equipo.setMarca(marca);
        equipo.setModelo(modelo);
        equipo.setNumeroSerie(numeroSerie);
        equipo.setPotenciaKva(potenciaKva);
        equipo.setTensiones(tensiones);
        equipo.setAnoFabricacion(ano);
        equipo.setPosicionTap(tap);
        equipo.setGrupoConexion(grupo);
        equipo.setIdInstalacion(idInstalacion);
        equipo.setInstalacion(instalacion);
        equipo.setDireccion(direccion);
        equipo.setLatitud(latitud);
        equipo.setLongitud(longitud);
        equipo.setIdCliente(idCliente);
        return equipo;
    }
}