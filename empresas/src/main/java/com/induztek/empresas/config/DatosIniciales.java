package com.induztek.empresas.config;

import com.induztek.empresas.model.Empresa;
import com.induztek.empresas.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;

    @Override
    public void run(String... args) {
        if (empresaRepository.count() == 0) {
            empresaRepository.saveAll(List.of(
                    Empresa.builder().nombre("Induztek Ingeniería SpA").direccion("Av. Providencia 1234, Providencia").build(),
                    Empresa.builder().nombre("Industrias Aurora S.A.").direccion("Camino Los Maitenes 1200, Lampa").build(),
                    Empresa.builder().nombre("Envases del Sur S.A.").direccion("Ruta 5 Sur Km 1020, Puerto Montt").build()
            ));
        }
    }
}