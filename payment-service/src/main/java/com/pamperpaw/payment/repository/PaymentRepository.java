package com.pamperpaw.payment.repository;

import com.pamperpaw.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByAppointmentId(Long appointmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findWithLockByAppointmentId(Long appointmentId);

    boolean existsByAppointmentIdAndPaymentStatus(Long appointmentId, com.pamperpaw.payment.entity.PaymentStatus status);
}
