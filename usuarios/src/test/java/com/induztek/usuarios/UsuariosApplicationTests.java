package com.induztek.usuarios;

import com.induztek.usuarios.model.Usuario;
import com.induztek.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:usuarios-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"jwt.secreto=usuarios-test-secret-key-that-is-at-least-32-bytes",
		"security.internal-token=usuarios-test-internal-token"
})
class UsuariosApplicationTests {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void usuarioSePersisteYSePuedeConsultarPorRut() {
		String rut = "1234567-" + java.util.UUID.randomUUID().toString().substring(0, 1);
		Usuario usuario = Usuario.builder()
				.nombre("Usuario")
				.apellido("Prueba")
				.rut(rut)
				.correo(java.util.UUID.randomUUID() + "@example.com")
				.telefono("1234567890")
				.rolId(2L)
				.build();
		usuarioRepository.save(usuario);

		assertTrue(usuarioRepository.existsByRut(rut));
	}
}
