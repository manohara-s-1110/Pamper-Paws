package com.pamperpaw.visit.feign;

import com.pamperpaw.visit.dto.PetDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    @GetMapping("/customers/{id}")
    Object getCustomerById(@PathVariable Long id);

    @GetMapping("/pets/{id}")
    PetDTO getPetById(@PathVariable Long id);
}
