package com.pamperpaw.payment.client;

import com.pamperpaw.payment.dto.UpdateVisitPaymentStatusRequest;
import com.pamperpaw.payment.dto.VisitResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "visit-service")
public interface VisitClient {

    @GetMapping("/visit/{id}")
    VisitResponseDTO getVisitById(@PathVariable Long id);

    @PatchMapping("/visit/{id}/payment-status")
    VisitResponseDTO updatePaymentStatus(@PathVariable Long id,
                                         @RequestBody UpdateVisitPaymentStatusRequest request);
}
