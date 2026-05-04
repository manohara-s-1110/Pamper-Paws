package com.pamperpaw.payment.service.impl;

import com.pamperpaw.payment.client.VisitClient;
import com.pamperpaw.payment.dto.PaymentInitiateRequest;
import com.pamperpaw.payment.dto.PaymentInitiateResponse;
import com.pamperpaw.payment.dto.PaymentResponse;
import com.pamperpaw.payment.dto.PaymentVerifyRequest;
import com.pamperpaw.payment.dto.VisitResponseDTO;
import com.pamperpaw.payment.entity.Payment;
import com.pamperpaw.payment.entity.PaymentMethod;
import com.pamperpaw.payment.entity.PaymentStatus;
import com.pamperpaw.payment.exception.PaymentException;
import com.pamperpaw.payment.exception.ResourceNotFoundException;
import com.pamperpaw.payment.repository.PaymentRepository;
import com.pamperpaw.payment.service.RazorpayGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private VisitClient visitClient;

    @Mock
    private RazorpayGateway razorpayGateway;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "rzp_test_key");
        ReflectionTestUtils.setField(paymentService, "currency", "INR");
    }

    @Test
    void initiateOnlineCreatesRazorpayOrderAndSavesPayment() {
        PaymentInitiateRequest request = request(PaymentMethod.ONLINE);
        VisitResponseDTO visit = visit();
        Payment saved = payment(PaymentStatus.PENDING);
        saved.setRazorpayOrderId("order_123");

        when(visitClient.getVisitById(10L)).thenReturn(visit);
        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.empty());
        when(razorpayGateway.createOrder(10L, BigDecimal.valueOf(500))).thenReturn("order_123");
        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        PaymentInitiateResponse response = paymentService.initiate(request);

        assertEquals("order_123", response.getRazorpayOrderId());
        assertEquals("rzp_test_key", response.getRazorpayKeyId());
        verify(razorpayGateway).createOrder(10L, BigDecimal.valueOf(500));
    }

    @Test
    void initiateCashSkipsRazorpayOrder() {
        PaymentInitiateRequest request = request(PaymentMethod.CASH);
        Payment saved = payment(PaymentStatus.PENDING);
        saved.setPaymentMethod(PaymentMethod.CASH);

        when(visitClient.getVisitById(10L)).thenReturn(visit());
        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        PaymentInitiateResponse response = paymentService.initiate(request);

        assertEquals(PaymentMethod.CASH, response.getPaymentMethod());
        verifyNoInteractions(razorpayGateway);
    }

    @Test
    void initiateReturnsPendingDuplicateForRetry() {
        Payment existing = payment(PaymentStatus.PENDING);
        existing.setRazorpayOrderId("order_retry");

        when(visitClient.getVisitById(10L)).thenReturn(visit());
        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(existing));
        when(razorpayGateway.findCapturedPaymentId("order_retry")).thenReturn(Optional.empty());

        PaymentInitiateResponse response = paymentService.initiate(request(PaymentMethod.ONLINE));

        assertEquals("order_retry", response.getRazorpayOrderId());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void initiateReconcilesPaidDuplicateForRetry() {
        Payment existing = payment(PaymentStatus.PENDING);
        existing.setRazorpayOrderId("order_paid");

        when(visitClient.getVisitById(10L)).thenReturn(visit());
        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(existing));
        when(razorpayGateway.findCapturedPaymentId("order_paid")).thenReturn(Optional.of("pay_paid"));
        when(paymentRepository.save(existing)).thenReturn(existing);

        PaymentInitiateResponse response = paymentService.initiate(request(PaymentMethod.ONLINE));

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals("pay_paid", existing.getTransactionId());
        verify(visitClient).updatePaymentStatus(eq(10L), any());
    }

    @Test
    void initiateReturnsSuccessfulDuplicateForRetry() {
        when(visitClient.getVisitById(10L)).thenReturn(visit());
        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(payment(PaymentStatus.SUCCESS)));

        PaymentInitiateResponse response = paymentService.initiate(request(PaymentMethod.ONLINE));

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        verify(visitClient).updatePaymentStatus(eq(10L), any());
    }

    @Test
    void initiateRejectsAmountMismatch() {
        PaymentInitiateRequest request = request(PaymentMethod.ONLINE);
        request.setAmount(BigDecimal.valueOf(700));

        when(visitClient.getVisitById(10L)).thenReturn(visit());

        assertThrows(PaymentException.class, () -> paymentService.initiate(request));
    }

    @Test
    void verifyMarksSuccessAndConfirmsVisit() {
        Payment payment = payment(PaymentStatus.PENDING);
        payment.setRazorpayOrderId("order_123");

        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(payment));
        when(razorpayGateway.verifySignature("order_123", "pay_123", "sig")).thenReturn(true);
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.verify(verifyRequest("order_123"));

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals("pay_123", response.getTransactionId());
        verify(visitClient).updatePaymentStatus(eq(10L), any());
    }

    @Test
    void verifyMarksFailedWhenSignatureInvalid() {
        Payment payment = payment(PaymentStatus.PENDING);
        payment.setRazorpayOrderId("order_123");

        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(payment));
        when(razorpayGateway.verifySignature("order_123", "pay_123", "sig")).thenReturn(false);
        when(razorpayGateway.findCapturedPaymentId("order_123")).thenReturn(Optional.empty());
        when(paymentRepository.save(payment)).thenReturn(payment);

        assertThrows(PaymentException.class, () -> paymentService.verify(verifyRequest("order_123")));
        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());
    }

    @Test
    void verifyReconcilesCapturedPaymentWhenSignatureCheckFails() {
        Payment payment = payment(PaymentStatus.PENDING);
        payment.setRazorpayOrderId("order_123");

        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(payment));
        when(razorpayGateway.verifySignature("order_123", "pay_123", "sig")).thenReturn(false);
        when(razorpayGateway.findCapturedPaymentId("order_123")).thenReturn(Optional.of("pay_captured"));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse response = paymentService.verify(verifyRequest("order_123"));

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals("pay_captured", response.getTransactionId());
        verify(visitClient).updatePaymentStatus(eq(10L), any());
    }

    @Test
    void verifyReturnsAlreadySuccessfulPayment() {
        Payment payment = payment(PaymentStatus.SUCCESS);
        payment.setTransactionId("pay_done");

        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.verify(verifyRequest("order_123"));

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals("pay_done", response.getTransactionId());
        verifyNoInteractions(razorpayGateway);
    }

    @Test
    void verifyRejectsCashPayment() {
        Payment payment = payment(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.CASH);

        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(payment));

        assertThrows(PaymentException.class, () -> paymentService.verify(verifyRequest("order_123")));
    }

    @Test
    void verifyMarksFailedWhenOrderDoesNotMatch() {
        Payment payment = payment(PaymentStatus.PENDING);
        payment.setRazorpayOrderId("order_actual");

        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        assertThrows(PaymentException.class, () -> paymentService.verify(verifyRequest("order_wrong")));
        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());
    }

    @Test
    void verifyThrowsWhenPaymentMissing() {
        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.verify(verifyRequest("order_123")));
    }

    @Test
    void getByAppointmentIdReturnsPayment() {
        when(paymentRepository.findByAppointmentId(10L)).thenReturn(Optional.of(payment(PaymentStatus.PENDING)));

        PaymentResponse response = paymentService.getByAppointmentId(10L);

        assertEquals(10L, response.getAppointmentId());
        assertEquals(BigDecimal.valueOf(500), response.getAmount());
    }

    @Test
    void getByAppointmentIdThrowsWhenMissing() {
        when(paymentRepository.findByAppointmentId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getByAppointmentId(99L));
    }

    @Test
    void initiateThrowsWhenVisitMissing() {
        when(visitClient.getVisitById(10L)).thenThrow(new RuntimeException("missing"));

        assertThrows(ResourceNotFoundException.class, () -> paymentService.initiate(request(PaymentMethod.ONLINE)));
    }

    @Test
    void initiateRejectsUserMismatch() {
        VisitResponseDTO visit = visit();
        visit.setCustomerId(99L);
        when(visitClient.getVisitById(10L)).thenReturn(visit);

        assertThrows(PaymentException.class, () -> paymentService.initiate(request(PaymentMethod.ONLINE)));
    }

    private PaymentInitiateRequest request(PaymentMethod method) {
        PaymentInitiateRequest request = new PaymentInitiateRequest();
        request.setAppointmentId(10L);
        request.setUserId(3L);
        request.setAmount(BigDecimal.valueOf(500));
        request.setPaymentMethod(method);
        return request;
    }

    private PaymentVerifyRequest verifyRequest(String orderId) {
        PaymentVerifyRequest request = new PaymentVerifyRequest();
        request.setAppointmentId(10L);
        request.setRazorpayOrderId(orderId);
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("sig");
        return request;
    }

    private VisitResponseDTO visit() {
        VisitResponseDTO visit = new VisitResponseDTO();
        visit.setId(10L);
        visit.setCustomerId(3L);
        visit.setVetId(5L);
        visit.setConsultationFee(BigDecimal.valueOf(500));
        return visit;
    }

    private Payment payment(PaymentStatus status) {
        return Payment.builder()
                .id(1L)
                .appointmentId(10L)
                .userId(3L)
                .amount(BigDecimal.valueOf(500))
                .paymentMethod(PaymentMethod.ONLINE)
                .paymentStatus(status)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
