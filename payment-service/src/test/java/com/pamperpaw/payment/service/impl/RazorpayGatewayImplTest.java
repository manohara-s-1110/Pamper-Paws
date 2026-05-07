package com.pamperpaw.payment.service.impl;

import com.pamperpaw.payment.exception.PaymentException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RazorpayGatewayImplTest {

    @Test
    void createOrderThrowsWhenCredentialsMissing() {
        RazorpayGatewayImpl gateway = new RazorpayGatewayImpl();
        ReflectionTestUtils.setField(gateway, "keyId", "");
        ReflectionTestUtils.setField(gateway, "keySecret", "");
        ReflectionTestUtils.setField(gateway, "currency", "INR");

        assertThrows(PaymentException.class, () -> gateway.createOrder(1L, BigDecimal.valueOf(500)));
    }

    @Test
    void verifySignatureReturnsFalseForInvalidPayload() {
        RazorpayGatewayImpl gateway = new RazorpayGatewayImpl();
        ReflectionTestUtils.setField(gateway, "keySecret", "secret");

        assertFalse(gateway.verifySignature("order", "payment", "bad-signature"));
    }

    @Test
    void findCapturedPaymentThrowsWhenCredentialsMissing() {
        RazorpayGatewayImpl gateway = new RazorpayGatewayImpl();
        ReflectionTestUtils.setField(gateway, "keyId", "");
        ReflectionTestUtils.setField(gateway, "keySecret", "");

        assertThrows(PaymentException.class, () -> gateway.findCapturedPaymentId("order"));
    }

    @Test
    void findCapturedPaymentReturnsEmptyWhenLookupFails() {
        RazorpayGatewayImpl gateway = new RazorpayGatewayImpl();
        ReflectionTestUtils.setField(gateway, "keyId", "key");
        ReflectionTestUtils.setField(gateway, "keySecret", "secret");

        assertTrue(gateway.findCapturedPaymentId("order_missing").isEmpty());
    }

    @Test
    void isPaymentCapturedReturnsFalseForInvalidPaymentId() {
        RazorpayGatewayImpl gateway = new RazorpayGatewayImpl();
        ReflectionTestUtils.setField(gateway, "keyId", "key");
        ReflectionTestUtils.setField(gateway, "keySecret", "secret");

        assertFalse(gateway.isPaymentCaptured("order_123"));
    }
    
    @Test
    void verifySignatureReturnsFalseWhenSecretMissing() {

        RazorpayGatewayImpl gateway =
                new RazorpayGatewayImpl();

        ReflectionTestUtils.setField(
                gateway,
                "keySecret",
                "");

        assertFalse(
                gateway.verifySignature(
                        "order",
                        "payment",
                        "signature"));
    }

    @Test
    void createOrderThrowsWhenCurrencyMissing() {

        RazorpayGatewayImpl gateway =
                new RazorpayGatewayImpl();

        ReflectionTestUtils.setField(
                gateway,
                "keyId",
                "key");

        ReflectionTestUtils.setField(
                gateway,
                "keySecret",
                "secret");

        ReflectionTestUtils.setField(
                gateway,
                "currency",
                null);

        assertThrows(
                Exception.class,
                () -> gateway.createOrder(
                        1L,
                        BigDecimal.valueOf(500)));
    }

    @Test
    void isPaymentCapturedReturnsFalseWhenCredentialsMissing() {

        RazorpayGatewayImpl gateway =
                new RazorpayGatewayImpl();

        ReflectionTestUtils.setField(
                gateway,
                "keyId",
                "");

        ReflectionTestUtils.setField(
                gateway,
                "keySecret",
                "");

        assertFalse(
                gateway.isPaymentCaptured(
                        "payment_123"));
    }
}
