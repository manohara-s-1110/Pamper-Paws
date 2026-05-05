package com.pamperpaw.visit.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "visit", uniqueConstraints = {
        @UniqueConstraint(name = "uk_visit_vet_date_slot", columnNames = {"vetId", "visitDate", "timeSlot"})
})
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private VisitStatus status = VisitStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private PaymentMethod paymentMethod = PaymentMethod.ONLINE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee = BigDecimal.ZERO;
}
