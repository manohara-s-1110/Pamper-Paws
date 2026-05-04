package com.pamperpaw.payment.repository;

import com.pamperpaw.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentIdAndPaymentStatus(Long appointmentId, com.pamperpaw.payment.entity.PaymentStatus status);
}
