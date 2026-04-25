package com.pamperpaw.vet.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetDTO {

    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @Min(value = 0, message = "Experience must be positive")
    private int experience;

    private String phone;

    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Clinic address is required")
    private String clinicAddress;

    @NotBlank(message = "Available days are required")
    private String availableDays;

    @NotBlank(message = "Available time is required")
    private String availableTime;
}
