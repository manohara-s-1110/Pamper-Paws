package com.pamperpaw.payment.controller;

import com.pamperpaw.payment.dto.PaymentInitiateRequest;
import com.pamperpaw.payment.dto.PaymentInitiateResponse;
import com.pamperpaw.payment.dto.PaymentResponse;
import com.pamperpaw.payment.dto.PaymentVerifyRequest;
import com.pamperpaw.payment.entity.PaymentMethod;
import com.pamperpaw.payment.entity.PaymentStatus;
import com.pamperpaw.payment.service.PaymentService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentControllerTest {

    private final PaymentService paymentService = mock(PaymentService.class);
    private final PaymentController controller = new PaymentController(paymentService);

    @Test
    void initiateDelegatesToService() {
        PaymentInitiateRequest request = new PaymentInitiateRequest();
        PaymentInitiateResponse expected = PaymentInitiateResponse.builder()
                .appointmentId(1L)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(paymentService.initiate(request)).thenReturn(expected);

        assertEquals(expected, controller.initiate(request));
    }

    @Test
    void verifyDelegatesToService() {
        PaymentVerifyRequest request = new PaymentVerifyRequest();
        PaymentResponse expected = PaymentResponse.builder()
                .appointmentId(1L)
                .amount(BigDecimal.valueOf(500))
                .paymentMethod(PaymentMethod.ONLINE)
                .build();

        when(paymentService.verify(request)).thenReturn(expected);

        assertEquals(expected, controller.verify(request));
    }

    @Test
    void getPaymentDelegatesToService() {
        PaymentResponse expected = PaymentResponse.builder().appointmentId(7L).build();

        when(paymentService.getByAppointmentId(7L)).thenReturn(expected);

        assertEquals(expected, controller.getPayment(7L));
    }
}
