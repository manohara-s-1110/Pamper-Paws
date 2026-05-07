package com.pamperpaw.payment.service.impl;

import com.pamperpaw.payment.exception.PaymentException;
import com.pamperpaw.payment.service.RazorpayGateway;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RazorpayGatewayImpl implements RazorpayGateway {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.currency}")
    private String currency;

    @Override
    public String createOrder(Long appointmentId, BigDecimal amount) {
        if (!StringUtils.hasText(keyId) || !StringUtils.hasText(keySecret)) {
            throw new PaymentException("Razorpay credentials are not configured");
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact());
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "visit_" + appointmentId);

            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (Exception ex) {
            throw new PaymentException("Unable to create Razorpay order");
        }
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public Optional<String> findCapturedPaymentId(String orderId) {
        if (!StringUtils.hasText(keyId) || !StringUtils.hasText(keySecret)) {
            throw new PaymentException("Razorpay credentials are not configured");
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
            List<com.razorpay.Payment> payments = razorpayClient.orders.fetchPayments(orderId);
            return payments.stream()
                    .peek(payment -> log.info("Fetched Razorpay payment for orderId={} paymentId={} status={}",
                            orderId, payment.get("id"), payment.get("status")))
                    .filter(payment -> "captured".equalsIgnoreCase(String.valueOf(payment.get("status"))))
                    .map(payment -> String.valueOf(payment.get("id")))
                    .findFirst();
        } catch (Exception ex) {
            log.warn("Unable to fetch captured Razorpay payment for orderId={}", orderId, ex);
            return Optional.empty();
        }
    }

    @Override
    public boolean isPaymentCaptured(String paymentId) {
        if (!StringUtils.hasText(keyId) || !StringUtils.hasText(keySecret)) {
            throw new PaymentException("Razorpay credentials are not configured");
        }
        if (!StringUtils.hasText(paymentId) || !paymentId.startsWith("pay_")) {
            return false;
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
            com.razorpay.Payment payment = razorpayClient.payments.fetch(paymentId);
            String status = String.valueOf(payment.get("status"));
            log.info("Fetched Razorpay payment status paymentId={} status={}", paymentId, status);
            return "captured".equalsIgnoreCase(status);
        } catch (Exception ex) {
            log.warn("Unable to verify Razorpay payment status paymentId={}", paymentId, ex);
            return false;
        }
    }

    @Override
    public String refundPayment(String paymentId, BigDecimal amount) {
        if (!StringUtils.hasText(keyId) || !StringUtils.hasText(keySecret)) {
            throw new PaymentException("Razorpay credentials are not configured");
        }
        if (!StringUtils.hasText(paymentId) || !paymentId.startsWith("pay_")) {
            throw new PaymentException("Razorpay payment id is missing");
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
            JSONObject refundRequest = new JSONObject();
            long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
            refundRequest.put("amount", amountInPaise);
            log.info("Sending Razorpay refund request paymentId={} payload={}", paymentId, refundRequest);

            Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);
            log.info("Received Razorpay refund response paymentId={} refundId={} status={}",
                    paymentId, refund.get("id"), refund.get("status"));
            return refund.get("id");
        } catch (PaymentException ex) {
            throw ex;
        } catch (RazorpayException ex) {
            throw new PaymentException("Unable to process Razorpay refund: " + ex.getMessage());
        } catch (Exception ex) {
            throw new PaymentException("Unable to process Razorpay refund: " + ex.getMessage());
        }
    }
}
