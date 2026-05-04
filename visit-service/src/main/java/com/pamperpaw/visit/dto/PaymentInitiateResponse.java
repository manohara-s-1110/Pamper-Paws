package com.pamperpaw.visit.dto;

import com.pamperpaw.visit.entity.PaymentMethod;
import com.pamperpaw.visit.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentInitiateResponse {
    private Long paymentId;
    private Long appointmentId;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private String currency;
}
