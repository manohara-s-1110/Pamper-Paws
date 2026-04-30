package com.pamperpaw.visit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VetLeaveRequestDTO {

    @NotNull(message = "Vet ID is required")
    private Long vetId;

    @NotBlank(message = "Leave date is required")
    private String date;
}
