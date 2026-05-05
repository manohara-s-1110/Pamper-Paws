package com.pamperpaw.payment.service;

import com.pamperpaw.payment.dto.PaymentInitiateRequest;
import com.pamperpaw.payment.dto.PaymentInitiateResponse;
import com.pamperpaw.payment.dto.PaymentResponse;
import com.pamperpaw.payment.dto.PaymentVerifyRequest;

import java.util.concurrent.CompletableFuture;

public interface PaymentService {

    PaymentInitiateResponse initiate(PaymentInitiateRequest request);

    PaymentResponse verify(PaymentVerifyRequest request);

    PaymentResponse getByAppointmentId(Long appointmentId);

    PaymentResponse refund(Long appointmentId);

    CompletableFuture<PaymentResponse> getByAppointmentIdAsync(Long appointmentId);
}
