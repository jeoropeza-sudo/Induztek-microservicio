package com.induztek.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"jwt.secreto=gateway-test-secret-key-that-is-at-least-32-bytes",
		"security.internal-token=gateway-test-internal-token"
})
class GatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
