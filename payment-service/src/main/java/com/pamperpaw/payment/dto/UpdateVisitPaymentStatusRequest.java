package com.pamperpaw.payment.dto;

import com.pamperpaw.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateVisitPaymentStatusRequest {
    private PaymentStatus paymentStatus;
}
