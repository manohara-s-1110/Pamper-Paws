package com.pamperpaw.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "jwt.secret=test-secret-key-for-api-gateway-1234567890"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
