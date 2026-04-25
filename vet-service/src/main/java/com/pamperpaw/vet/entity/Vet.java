package com.pamperpaw.vet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(name = "vets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Vet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @Min(value = 0, message = "Experience must be positive")
    private int experience;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Clinic address is required")
    private String clinicAddress;

    @NotBlank(message = "Available days are required")
    private String availableDays;

    @NotBlank(message = "Available time is required")
    private String availableTime;
}
