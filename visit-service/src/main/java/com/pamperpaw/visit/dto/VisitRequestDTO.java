package com.pamperpaw.visit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.pamperpaw.visit.entity.PaymentMethod;

@Data
public class VisitRequestDTO {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Vet ID is required")
    private Long vetId;

    @NotNull(message = "Pet ID is required")
    private Long petId;

    @NotBlank(message = "Visit date is required")
    private String visitDate;

    @NotBlank(message = "Reason is required")
    private String reason;
    
    @NotBlank(message = "Time slot is required")
    private String timeSlot;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod = PaymentMethod.ONLINE;
}
