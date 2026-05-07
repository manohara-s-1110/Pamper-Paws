package com.pamperpaw.payment.dto;

import com.pamperpaw.payment.entity.PaymentMethod;
import com.pamperpaw.payment.entity.PaymentStatus;
import com.pamperpaw.payment.entity.RefundStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

    private Long id;
    private Long appointmentId;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private String razorpayOrderId;
    private String refundId;
    private String refundTransactionId;
    private RefundStatus refundStatus;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
}
