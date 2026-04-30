package com.pamperpaw.visit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        name = "vet_leaves",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vet_leave_date",
                columnNames = {"vet_id", "leave_date"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Vet ID is required")
    @Column(name = "vet_id", nullable = false)
    private Long vetId;

    @NotBlank(message = "Leave date is required")
    @Column(name = "leave_date", nullable = false)
    private String date;
}
