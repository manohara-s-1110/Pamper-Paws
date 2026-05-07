package com.pamperpaw.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DiscoveryServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void applicationCanBeConstructed() {
        DiscoveryServerApplication application =
                assertDoesNotThrow(DiscoveryServerApplication::new);

        assertNotNull(application);
    }

    @Test
    void mainMethodRunsSuccessfully() {
        assertDoesNotThrow(() ->
                DiscoveryServerApplication.main(new String[]{})
        );
    }
}