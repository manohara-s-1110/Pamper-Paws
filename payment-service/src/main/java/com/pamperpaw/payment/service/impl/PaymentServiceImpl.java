package com.pamperpaw.payment.service.impl;

import com.pamperpaw.payment.client.VisitClient;
import com.pamperpaw.payment.dto.*;
import com.pamperpaw.payment.entity.Payment;
import com.pamperpaw.payment.entity.PaymentMethod;
import com.pamperpaw.payment.entity.PaymentStatus;
import com.pamperpaw.payment.exception.PaymentException;
import com.pamperpaw.payment.exception.ResourceNotFoundException;
import com.pamperpaw.payment.repository.PaymentRepository;
import com.pamperpaw.payment.service.PaymentService;
import com.pamperpaw.payment.service.RazorpayGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final VisitClient visitClient;
    private final RazorpayGateway razorpayGateway;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.currency}")
    private String currency;

    @Override
    @Transactional
    public PaymentInitiateResponse initiate(PaymentInitiateRequest request) {
        VisitResponseDTO visit = loadVisit(request.getAppointmentId());
        validateRequestMatchesVisit(request, visit);

        Payment existing = paymentRepository.findByAppointmentId(request.getAppointmentId()).orElse(null);
        if (existing != null) {
            if (PaymentStatus.SUCCESS.equals(existing.getPaymentStatus())) {
                syncVisitPaymentStatus(existing);
                return toInitiateResponse(existing);
            }
            Payment reconciled = reconcileCapturedRazorpayPayment(existing);
            if (PaymentStatus.SUCCESS.equals(reconciled.getPaymentStatus())) {
                return toInitiateResponse(reconciled);
            }
            if (PaymentMethod.ONLINE.equals(request.getPaymentMethod())) {
                reconciled.setRazorpayOrderId(razorpayGateway.createOrder(request.getAppointmentId(), request.getAmount()));
                Payment saved = paymentRepository.save(reconciled);
                log.info("Refreshed Razorpay order for pending payment id={} appointmentId={}", saved.getId(), saved.getAppointmentId());
                return toInitiateResponse(saved);
            }
            return toInitiateResponse(reconciled);
        }

        Payment payment = Payment.builder()
                .appointmentId(request.getAppointmentId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        if (PaymentMethod.ONLINE.equals(request.getPaymentMethod())) {
            payment.setRazorpayOrderId(razorpayGateway.createOrder(request.getAppointmentId(), request.getAmount()));
        }

        Payment saved = paymentRepository.save(payment);
        log.info("Initiated {} payment id={} appointmentId={}", saved.getPaymentMethod(), saved.getId(), saved.getAppointmentId());
        return toInitiateResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse verify(PaymentVerifyRequest request) {
        Payment payment = paymentRepository.findByAppointmentId(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for appointment: " + request.getAppointmentId()));

        if (PaymentStatus.SUCCESS.equals(payment.getPaymentStatus())) {
            return toResponse(payment);
        }

        if (!PaymentMethod.ONLINE.equals(payment.getPaymentMethod())) {
            throw new PaymentException("Only online payments can be verified");
        }

        if (!request.getRazorpayOrderId().equals(payment.getRazorpayOrderId())) {
            markFailed(payment);
            throw new PaymentException("Razorpay order does not match payment record");
        }

        if (!razorpayGateway.verifySignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            Payment reconciled = reconcileCapturedRazorpayPayment(payment);
            if (PaymentStatus.SUCCESS.equals(reconciled.getPaymentStatus())) {
                return toResponse(reconciled);
            }
            markFailed(payment);
            throw new PaymentException("Payment signature verification failed");
        }

        Payment saved = markSuccess(payment, request.getRazorpayPaymentId());
        log.info("Payment verified appointmentId={} paymentId={}", saved.getAppointmentId(), saved.getId());
        return toResponse(saved);
    }

    @Override
    public PaymentResponse getByAppointmentId(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for appointment: " + appointmentId));
    }

    @Async
    @Override
    public CompletableFuture<PaymentResponse> getByAppointmentIdAsync(Long appointmentId) {
        return CompletableFuture.completedFuture(getByAppointmentId(appointmentId));
    }

    private VisitResponseDTO loadVisit(Long appointmentId) {
        try {
            return visitClient.getVisitById(appointmentId);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Appointment not found with id: " + appointmentId);
        }
    }

    private void validateRequestMatchesVisit(PaymentInitiateRequest request, VisitResponseDTO visit) {
        if (!request.getUserId().equals(visit.getCustomerId())) {
            throw new PaymentException("Payment user does not match appointment customer");
        }
        if (visit.getConsultationFee() == null || request.getAmount().compareTo(visit.getConsultationFee()) != 0) {
            throw new PaymentException("Payment amount does not match appointment consultation fee");
        }
    }

    private void markFailed(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }

    private Payment reconcileCapturedRazorpayPayment(Payment payment) {
        if (!PaymentMethod.ONLINE.equals(payment.getPaymentMethod())
                || !StringUtils.hasText(payment.getRazorpayOrderId())
                || PaymentStatus.SUCCESS.equals(payment.getPaymentStatus())) {
            return payment;
        }

        return razorpayGateway.findCapturedPaymentId(payment.getRazorpayOrderId())
                .map(paymentId -> {
                    log.info("Reconciled captured Razorpay payment appointmentId={} paymentId={}",
                            payment.getAppointmentId(), paymentId);
                    return markSuccess(payment, paymentId);
                })
                .orElse(payment);
    }

    private Payment markSuccess(Payment payment, String transactionId) {
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);
        Payment saved = paymentRepository.save(payment);
        syncVisitPaymentStatus(saved);
        return saved;
    }

    private void syncVisitPaymentStatus(Payment payment) {
        try {
            visitClient.updatePaymentStatus(payment.getAppointmentId(), new UpdateVisitPaymentStatusRequest(PaymentStatus.SUCCESS));
        } catch (Exception ex) {
            log.warn("Payment succeeded but visit payment status sync failed appointmentId={}",
                    payment.getAppointmentId(), ex);
        }
    }

    private PaymentInitiateResponse toInitiateResponse(Payment payment) {
        return PaymentInitiateResponse.builder()
                .paymentId(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayKeyId(razorpayKeyId)
                .currency(currency)
                .build();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
