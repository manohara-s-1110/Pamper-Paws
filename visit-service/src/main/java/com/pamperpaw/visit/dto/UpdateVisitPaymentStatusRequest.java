package com.pamperpaw.visit.dto;

import com.pamperpaw.visit.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVisitPaymentStatusRequest {
    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
}
