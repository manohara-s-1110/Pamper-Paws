package com.pamperpaw.payment.service;

import java.math.BigDecimal;
import java.util.Optional;

public interface RazorpayGateway {

    String createOrder(Long appointmentId, BigDecimal amount);

    boolean verifySignature(String orderId, String paymentId, String signature);

    Optional<String> findCapturedPaymentId(String orderId);
}
