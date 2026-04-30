package com.pamperpaw.visit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        name = "visit",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_visit_vet_date_slot",
                columnNames = {"vet_id", "visit_date", "time_slot"}
        )
)
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
    @Column(name = "vet_id", nullable = false)
    private Long vetId;

    @NotNull(message = "Pet ID is required")
    private Long petId;

    @NotBlank(message = "Visit date is required")
    @Column(name = "visit_date", nullable = false)
    private String visitDate;
    @NotBlank(message = "Reason is required")
    private String reason;
    
    @NotBlank(message = "Time slot is required")
    @Column(name = "time_slot", nullable = false)
    private String timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisitStatus status = VisitStatus.PENDING;
}
