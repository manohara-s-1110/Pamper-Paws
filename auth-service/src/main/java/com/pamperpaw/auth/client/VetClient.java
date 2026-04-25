package com.pamperpaw.auth.client;

import com.pamperpaw.auth.dto.VetProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "vet-service")
public interface VetClient {

    @PostMapping("/vets")
    Object createVet(@RequestBody VetProfileRequest request);

    @GetMapping("/vets/username/{username}")
    Object getVetByUsername(@PathVariable String username);
}
