package com.pamperpaw.visit.feign;

import com.pamperpaw.visit.dto.VetFeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "vet-service")
public interface VetClient {

    @GetMapping("/vets/{id}")
    Object getVetById(@PathVariable Long id);

    @GetMapping("/vets/{id}/fee")
    VetFeeResponse getVetFee(@PathVariable Long id);

    @GetMapping("/vets/{id}/leaves")
    List<String> getLeaveDates(@PathVariable Long id);
}
