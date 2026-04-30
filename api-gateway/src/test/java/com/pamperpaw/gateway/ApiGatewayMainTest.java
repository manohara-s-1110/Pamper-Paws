package com.pamperpaw.gateway;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ApiGatewayMainTest {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
            ApiGatewayApplication.main(new String[]{"arg"});

            springApplication.verify(() -> SpringApplication.run(ApiGatewayApplication.class, new String[]{"arg"}));
        }
    }
}
