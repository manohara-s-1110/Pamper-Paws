package com.pamperpaw.visit.dto;

import lombok.Data;
import com.pamperpaw.visit.entity.PaymentMethod;
import com.pamperpaw.visit.entity.PaymentStatus;
import com.pamperpaw.visit.entity.VisitStatus;

import java.math.BigDecimal;

@Data
public class VisitResponseDTO {

    private Long id;
    private Long customerId;
    private Long vetId;
    private Long petId;
    private String petName;
    private String visitDate;
    private String reason;
    private String timeSlot;
    private VisitStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal consultationFee;
    private PaymentInitiateResponse payment;
}
