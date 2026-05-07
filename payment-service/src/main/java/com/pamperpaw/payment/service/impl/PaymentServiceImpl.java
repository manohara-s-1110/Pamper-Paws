package com.pamperpaw.payment.service.impl;

import com.pamperpaw.payment.client.VisitClient;
import com.pamperpaw.payment.dto.*;
import com.pamperpaw.payment.entity.Payment;
import com.pamperpaw.payment.entity.PaymentMethod;
import com.pamperpaw.payment.entity.PaymentStatus;
import com.pamperpaw.payment.entity.RefundStatus;
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

import java.time.LocalDateTime;
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
            if (PaymentStatus.SUCCESS.equals(existing.getPaymentStatus()) || PaymentStatus.REFUNDED.equals(existing.getPaymentStatus())) {
                syncVisitPaymentStatus(existing, existing.getPaymentStatus());
                return toInitiateResponse(existing);
            }
            Payment reconciled = reconcileCapturedRazorpayPayment(existing);
            if (PaymentStatus.SUCCESS.equals(reconciled.getPaymentStatus())) {
                return toInitiateResponse(reconciled);
            }
            if (PaymentMethod.ONLINE.equals(request.getPaymentMethod()) && !StringUtils.hasText(reconciled.getRazorpayOrderId())) {
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

        if (PaymentStatus.SUCCESS.equals(payment.getPaymentStatus()) || PaymentStatus.REFUNDED.equals(payment.getPaymentStatus())) {
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

    @Override
    @Transactional
    public PaymentResponse refund(Long appointmentId) {
        Payment payment = paymentRepository.findWithLockByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for appointment: " + appointmentId));

        if (PaymentStatus.REFUNDED.equals(payment.getPaymentStatus()) || RefundStatus.SUCCESS.equals(payment.getRefundStatus())) {
            log.info("Refund already completed appointmentId={} refundId={}", appointmentId, payment.getRefundId());
            return toResponse(payment);
        }

        if (!PaymentMethod.ONLINE.equals(payment.getPaymentMethod())) {
            payment.setRefundStatus(RefundStatus.NOT_APPLICABLE);
            paymentRepository.save(payment);
            throw new PaymentException("Refund is not applicable for cash payments");
        }

        if (!PaymentStatus.SUCCESS.equals(payment.getPaymentStatus())) {
            throw new PaymentException("Only successful online payments can be refunded");
        }

        String refundPaymentId = resolveRefundPaymentId(payment);

        payment.setRefundStatus(RefundStatus.PENDING);
        paymentRepository.save(payment);
        log.info("Starting full Razorpay refund appointmentId={} orderId={} paymentId={} paymentStatus={} amount={}",
                payment.getAppointmentId(), payment.getRazorpayOrderId(), refundPaymentId, payment.getPaymentStatus(), payment.getAmount());

        try {
            String refundId = razorpayGateway.refundPayment(refundPaymentId, payment.getAmount());
            payment.setRefundId(refundId);
            payment.setRefundTransactionId(refundId);
            payment.setRefundStatus(RefundStatus.SUCCESS);
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            payment.setRefundedAt(LocalDateTime.now());
            Payment saved = paymentRepository.save(payment);
            syncVisitPaymentStatus(saved, PaymentStatus.REFUNDED);
            log.info("Razorpay refund successful appointmentId={} refundId={}", saved.getAppointmentId(), refundId);
            return toResponse(saved);
        } catch (PaymentException ex) {
            payment.setRefundStatus(RefundStatus.FAILED);
            paymentRepository.save(payment);
            log.warn("Razorpay refund failed appointmentId={} orderId={} paymentId={} paymentStatus={}",
                    payment.getAppointmentId(), payment.getRazorpayOrderId(), refundPaymentId, payment.getPaymentStatus(), ex);
            throw ex;
        }
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

    private String resolveRefundPaymentId(Payment payment) {
        String storedPaymentId = payment.getTransactionId();
        log.info("Resolving refund payment id appointmentId={} orderId={} storedPaymentId={} paymentStatus={}",
                payment.getAppointmentId(), payment.getRazorpayOrderId(), storedPaymentId, payment.getPaymentStatus());

        if (StringUtils.hasText(storedPaymentId) && storedPaymentId.startsWith("pay_")) {
            if (razorpayGateway.isPaymentCaptured(storedPaymentId)) {
                return storedPaymentId;
            }
            log.warn("Stored Razorpay payment id is not captured appointmentId={} paymentId={}",
                    payment.getAppointmentId(), storedPaymentId);
        }

        if (StringUtils.hasText(payment.getRazorpayOrderId())) {
            return razorpayGateway.findCapturedPaymentId(payment.getRazorpayOrderId())
                    .map(capturedPaymentId -> {
                        payment.setTransactionId(capturedPaymentId);
                        paymentRepository.save(payment);
                        log.info("Updated payment record with captured Razorpay payment id appointmentId={} orderId={} paymentId={}",
                                payment.getAppointmentId(), payment.getRazorpayOrderId(), capturedPaymentId);
                        return capturedPaymentId;
                    })
                    .orElseThrow(() -> new PaymentException("No captured Razorpay payment found for this appointment"));
        }

        throw new PaymentException("Valid Razorpay payment id is missing for this appointment");
    }

    private Payment markSuccess(Payment payment, String transactionId) {
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);
        Payment saved = paymentRepository.save(payment);
        syncVisitPaymentStatus(saved);
        return saved;
    }

    private void syncVisitPaymentStatus(Payment payment) {
        syncVisitPaymentStatus(payment, PaymentStatus.SUCCESS);
    }

    private void syncVisitPaymentStatus(Payment payment, PaymentStatus status) {
        try {
            visitClient.updatePaymentStatus(payment.getAppointmentId(), new UpdateVisitPaymentStatusRequest(status));
        } catch (Exception ex) {
            log.warn("Payment status sync failed appointmentId={} status={}",
                    payment.getAppointmentId(), status, ex);
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
                .refundId(payment.getRefundId())
                .refundTransactionId(payment.getRefundTransactionId())
                .refundStatus(payment.getRefundStatus())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
