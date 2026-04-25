package com.pamperpaw.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VetRegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @Min(value = 0, message = "Experience must be positive")
    private int experience;

    @NotBlank(message = "Clinic address is required")
    private String clinicAddress;

    @NotBlank(message = "Available days are required")
    private String availableDays;

    @NotBlank(message = "Available time is required")
    private String availableTime;
}
