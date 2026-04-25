package com.pamperpaw.visit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
}
