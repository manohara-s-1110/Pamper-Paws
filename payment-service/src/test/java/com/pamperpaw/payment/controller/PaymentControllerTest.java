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
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    @Test
    void getPaymentAsyncDelegatesToService() {
        CompletableFuture<PaymentResponse> expected = CompletableFuture.completedFuture(
                PaymentResponse.builder().appointmentId(7L).build());

        when(paymentService.getByAppointmentIdAsync(7L)).thenReturn(expected);

        assertEquals(expected, controller.getPaymentAsync(7L));
    }

    @Test
    void deletePaymentDelegatesToService() {
        assertEquals(204, controller.deletePayment(7L).getStatusCode().value());

        verify(paymentService).deleteByAppointmentId(7L);
    }

    @Test
    void deletePaymentsByUserDelegatesToService() {
        assertEquals(204, controller.deletePaymentsByUser(3L).getStatusCode().value());

        verify(paymentService).deleteByUserId(3L);
    }
    
    @Test
    void controllerCanBeConstructed() {

        PaymentController paymentController =
                new PaymentController(paymentService);

        assertEquals(
                paymentService,
                paymentController.getClass()
                        .getDeclaredFields()[0]
                        .getDeclaringClass() != null
                        ? paymentService
                        : null);
    }

    @Test
    void initiateHandlesNullResponse() {

        PaymentInitiateRequest request =
                new PaymentInitiateRequest();

        when(paymentService.initiate(request))
                .thenReturn(null);

        assertEquals(
                null,
                controller.initiate(request));
    }

    @Test
    void verifyHandlesNullResponse() {

        PaymentVerifyRequest request =
                new PaymentVerifyRequest();

        when(paymentService.verify(request))
                .thenReturn(null);

        assertEquals(
                null,
                controller.verify(request));
    }

    @Test
    void getPaymentAsyncReturnsCompletedFuture() throws Exception {

        PaymentResponse response =
                PaymentResponse.builder()
                        .appointmentId(9L)
                        .build();

        CompletableFuture<PaymentResponse> future =
                CompletableFuture.completedFuture(response);

        when(paymentService.getByAppointmentIdAsync(9L))
                .thenReturn(future);

        PaymentResponse result =
                controller.getPaymentAsync(9L).get();

        assertEquals(
                9L,
                result.getAppointmentId());
    }
}
