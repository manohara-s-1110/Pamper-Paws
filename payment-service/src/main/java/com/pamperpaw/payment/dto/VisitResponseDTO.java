package com.pamperpaw.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VisitResponseDTO {
    private Long id;
    private Long customerId;
    private Long vetId;
    private BigDecimal consultationFee;
    private String status;
    private String paymentStatus;
}
