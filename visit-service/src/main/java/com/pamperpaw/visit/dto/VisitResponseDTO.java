package com.pamperpaw.visit.dto;

import lombok.Data;

@Data
public class VisitResponseDTO {

    private Long id;
    private Long customerId;
    private Long vetId;
    private Long petId;
    private String visitDate;
    private String reason;
    private String timeSlot;
}
