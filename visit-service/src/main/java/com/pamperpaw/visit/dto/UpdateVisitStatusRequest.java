package com.pamperpaw.visit.dto;

import com.pamperpaw.visit.entity.VisitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVisitStatusRequest {

    @NotNull(message = "Appointment status is required")
    private VisitStatus status;
}
