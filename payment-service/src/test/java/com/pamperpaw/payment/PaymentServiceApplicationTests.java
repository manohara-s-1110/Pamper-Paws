package com.pamperpaw.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:paymentdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void applicationCanBeConstructed() {

        PaymentServiceApplication application =
                assertDoesNotThrow(
                        PaymentServiceApplication::new);

        assertNotNull(application);
    }

    @Test
    void mainMethodRunsSuccessfully() {

        assertDoesNotThrow(() ->
                PaymentServiceApplication.main(
                        new String[]{})
        );
    }
}