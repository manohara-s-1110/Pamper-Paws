package com.pamperpaw.visit.feign;

import com.pamperpaw.visit.dto.PaymentInitiateRequest;
import com.pamperpaw.visit.dto.PaymentInitiateResponse;
import com.pamperpaw.visit.dto.PaymentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/payments/initiate")
    PaymentInitiateResponse initiate(@RequestBody PaymentInitiateRequest request);

    @GetMapping("/payments/{appointmentId}")
    PaymentResponse getPayment(@PathVariable Long appointmentId);

    @DeleteMapping("/payments/{appointmentId}")
    void deletePayment(@PathVariable Long appointmentId);

    @DeleteMapping("/payments/user/{userId}")
    void deletePaymentsByUser(@PathVariable Long userId);

}
