package com.induztek.instrumentos;

import com.induztek.instrumentos.model.Instrumento;
import com.induztek.instrumentos.repository.InstrumentoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:instrumentos-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "eureka.client.enabled=false",
        "jwt.secreto=instrumentos-test-secret-key-that-is-at-least-32-bytes",
        "security.internal-token=instrumentos-test-internal-token"
})
class InstrumentosApplicationTests {

    @Autowired
    private InstrumentoRepository instrumentoRepository;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void instrumentoSePersisteYSePuedeConsultarPorSerie() {
        String serie = "SERIE-" + java.util.UUID.randomUUID();
        Instrumento instrumento = Instrumento.builder()
                .nombre("Instrumento de prueba")
                .numeroSerie(serie)
                .tipoPrueba("AISLACION")
                .build();
        instrumentoRepository.save(instrumento);

        assertTrue(instrumentoRepository.existsByNumeroSerieIgnoreCase(serie));
    }
}
