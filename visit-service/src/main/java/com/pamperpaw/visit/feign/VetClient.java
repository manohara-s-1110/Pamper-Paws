package com.pamperpaw.visit.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "vet-service")
public interface VetClient {

    @GetMapping("/vets/{id}")
    Object getVetById(@PathVariable Long id);
}
