package com.pamperpaw.vet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VetLeaveRequest {

    @NotBlank(message = "Leave date is required")
    private String date;
}
