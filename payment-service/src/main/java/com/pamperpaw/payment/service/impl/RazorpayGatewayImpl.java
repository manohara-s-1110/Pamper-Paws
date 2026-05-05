package com.pamperpaw.payment.service.impl;

import com.pamperpaw.payment.exception.PaymentException;
import com.pamperpaw.payment.service.RazorpayGateway;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
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
                    .filter(payment -> "captured".equalsIgnoreCase(String.valueOf(payment.get("status"))))
                    .map(payment -> String.valueOf(payment.get("id")))
                    .findFirst();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
