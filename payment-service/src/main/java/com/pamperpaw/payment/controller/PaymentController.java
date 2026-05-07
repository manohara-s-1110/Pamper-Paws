package com.pamperpaw.payment.controller;

import com.pamperpaw.payment.dto.PaymentInitiateRequest;
import com.pamperpaw.payment.dto.PaymentInitiateResponse;
import com.pamperpaw.payment.dto.PaymentResponse;
import com.pamperpaw.payment.dto.PaymentVerifyRequest;
import com.pamperpaw.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public PaymentInitiateResponse initiate(@Valid @RequestBody PaymentInitiateRequest request) {
        return paymentService.initiate(request);
    }

    @PostMapping("/verify")
    public PaymentResponse verify(@Valid @RequestBody PaymentVerifyRequest request) {
        return paymentService.verify(request);
    }

    @GetMapping("/{appointmentId}")
    public PaymentResponse getPayment(@PathVariable Long appointmentId) {
        return paymentService.getByAppointmentId(appointmentId);
    }

    @GetMapping("/{appointmentId}/async")
    public CompletableFuture<PaymentResponse> getPaymentAsync(@PathVariable Long appointmentId) {
        return paymentService.getByAppointmentIdAsync(appointmentId);
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long appointmentId) {
        paymentService.deleteByAppointmentId(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deletePaymentsByUser(@PathVariable Long userId) {
        paymentService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
