package com.induztek.empresas;

import com.induztek.empresas.model.Empresa;
import com.induztek.empresas.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:empresas-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"jwt.secreto=empresas-test-secret-key-that-is-at-least-32-bytes",
		"security.internal-token=empresas-test-internal-token"
})
class EmpresasApplicationTests {

	@Autowired
	private EmpresaRepository empresaRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void empresaSePersisteYSePuedeConsultarPorNombre() {
		String nombre = "Empresa " + java.util.UUID.randomUUID();
		Empresa empresa = Empresa.builder()
				.nombre(nombre)
				.rut("11.111.111-1")
				.direccion("Av. Central 123")
				.build();
		empresaRepository.save(empresa);

		assertTrue(empresaRepository.existsByNombreIgnoreCase(nombre));
	}
}
