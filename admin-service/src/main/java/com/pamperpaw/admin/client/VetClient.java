package com.pamperpaw.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.pamperpaw.admin.dto.VetDTO;

import java.util.List;

@FeignClient(name = "vet-service")
public interface VetClient {

    @GetMapping("/vets")
    List<VetDTO> getAllVets();

    @GetMapping("/vets/{id}")
    VetDTO getVetById(@PathVariable("id") Long id);

    @PutMapping("/vets/{id}")
    VetDTO updateVet(@PathVariable("id") Long id, @RequestBody VetDTO vet);

    @DeleteMapping("/vets/{id}")
    String deleteVet(@PathVariable("id") Long id);
}
