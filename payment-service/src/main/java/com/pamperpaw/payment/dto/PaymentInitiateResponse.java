package com.pamperpaw.payment.dto;

import com.pamperpaw.payment.entity.PaymentMethod;
import com.pamperpaw.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
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
