package com.pamperpaw.visit.dto;

import lombok.Data;

@Data
public class VetLeaveResponseDTO {
    private Long id;
    private Long vetId;
    private String date;
}
