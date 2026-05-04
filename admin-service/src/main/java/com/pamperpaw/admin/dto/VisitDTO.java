package com.pamperpaw.admin.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitDTO {

    private Long id;
    private Long customerId;
    private Long vetId;
    private Long petId;
    private String visitDate;
    private String reason;
    private String timeSlot;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private java.math.BigDecimal consultationFee;
}
