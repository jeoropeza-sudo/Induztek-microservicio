package com.induztek.auth;

import com.induztek.auth.model.Credencial;
import com.induztek.auth.repository.CredencialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:auth-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"jwt.secreto=auth-test-secret-key-that-is-at-least-32-bytes",
		"security.internal-token=auth-test-internal-token",
		"app.mail.from=test@example.com"
})
class AuthApplicationTests {

	@Autowired
	private CredencialRepository credencialRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void credencialSePersisteYSePuedeConsultarPorCorreo() {
		String correo = java.util.UUID.randomUUID() + "@example.com";
		Credencial credencial = Credencial.builder()
				.usuarioId(100L)
				.correo(correo)
				.contrasena("hash")
				.primerIngreso(true)
				.build();
		credencialRepository.save(credencial);

		assertTrue(credencialRepository.findByCorreo(correo).isPresent());
	}
}
