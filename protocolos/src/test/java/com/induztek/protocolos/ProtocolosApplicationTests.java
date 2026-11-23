package com.induztek.protocolos;

import com.induztek.protocolos.model.Protocolo;
import com.induztek.protocolos.repository.ProtocoloRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:protocolos-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"jwt.secreto=protocolos-test-secret-key-that-is-at-least-32-bytes",
		"security.internal-token=protocolos-test-internal-token"
})
class ProtocolosApplicationTests {

	@Autowired
	private ProtocoloRepository protocoloRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void protocoloSePersisteYSePuedeConsultarPorUuid() {
		String uuid = java.util.UUID.randomUUID().toString();
		Protocolo protocolo = new Protocolo();
		protocolo.setUuidSync(uuid);
		protocolo.setIdEquipo(25L);
		protocolo.setIdTecnico(30L);
		protocolo.setFechaHora(System.currentTimeMillis());
		protocolo.setTipoPrueba("AISLACION");
		protocolo.setResultado("APROBADO");
		protocolo.setEstado("REGISTRADO");
		protocoloRepository.save(protocolo);

		assertTrue(protocoloRepository.findByUuidSync(uuid).isPresent());
	}
}
