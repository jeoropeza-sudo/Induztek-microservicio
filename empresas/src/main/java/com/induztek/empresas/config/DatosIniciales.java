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
                    Empresa.builder().nombre("Induztek Ingeniería SpA").rut("76.123.456-7").giro("Ingeniería eléctrica").direccion("Av. Providencia 1234, Providencia").contactoNombre("Contacto Induztek").contactoCorreo("contacto@induztek.cl").contactoTelefono("+56911112222").build(),
                    Empresa.builder().nombre("Industrias Aurora S.A.").rut("76.234.567-8").giro("Manufactura").direccion("Camino Los Maitenes 1200, Lampa").contactoNombre("Contacto Aurora").contactoCorreo("contacto@aurora.cl").contactoTelefono("+56933334444").build(),
                    Empresa.builder().nombre("Envases del Sur S.A.").rut("76.345.678-9").giro("Envases").direccion("Ruta 5 Sur Km 1020, Puerto Montt").contactoNombre("Contacto Envases").contactoCorreo("contacto@envases.cl").contactoTelefono("+56955556666").build()
            ));
        }
    }
}