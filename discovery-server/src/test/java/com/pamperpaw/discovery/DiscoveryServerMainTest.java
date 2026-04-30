package com.pamperpaw.discovery;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class DiscoveryServerMainTest {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
            DiscoveryServerApplication.main(new String[]{"arg"});

            springApplication.verify(() -> SpringApplication.run(DiscoveryServerApplication.class, new String[]{"arg"}));
        }
    }
}
