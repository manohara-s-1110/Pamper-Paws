package com.pamperpaw.visit.dto;

import com.pamperpaw.visit.entity.PaymentMethod;
import com.pamperpaw.visit.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long appointmentId;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private String razorpayOrderId;
    private LocalDateTime createdAt;
}
