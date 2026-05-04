package com.pamperpaw.visit.dto;

import com.pamperpaw.visit.entity.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentInitiateRequest {
    private Long appointmentId;
    private Long userId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
}
