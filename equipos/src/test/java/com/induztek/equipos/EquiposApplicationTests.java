package com.induztek.equipos;

import com.induztek.equipos.model.Equipo;
import com.induztek.equipos.repository.EquipoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:equipos-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"jwt.secreto=equipos-test-secret-key-that-is-at-least-32-bytes",
		"security.internal-token=equipos-test-internal-token"
})
class EquiposApplicationTests {

	@Autowired
	private EquipoRepository equipoRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void equipoSePersisteYSePuedeConsultarPorInstalacion() {
		Equipo equipo = new Equipo();
		equipo.setCodigo("EQ-" + java.util.UUID.randomUUID());
		equipo.setTag("TAG-" + java.util.UUID.randomUUID());
		equipo.setTipo("Celda MT");
		equipo.setIdInstalacion(25L);
		equipo.setIdCliente(30L);
		equipoRepository.save(equipo);

		assertTrue(equipoRepository.findByIdInstalacion(25L)
				.stream()
				.anyMatch(registro -> equipo.getTag().equals(registro.getTag())));
	}
}
