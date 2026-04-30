package com.pamperpaw.customer.controller;

import com.pamperpaw.customer.dto.PetDTO;
import com.pamperpaw.customer.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pets")
public class PetController {

    private final PetService service;

    @PostMapping("/customer/{customerId}")
    public PetDTO addPet(@PathVariable Long customerId, @Valid
                         @RequestBody PetDTO dto) {
        return service.addPet(customerId, dto);
    }

    @GetMapping("/customer/{customerId}")
    public List<PetDTO> getPets(@PathVariable Long customerId) {
        return service.getPetsByCustomer(customerId);
    }
    
    @PutMapping("/{id}")
    public PetDTO updatePet(@PathVariable Long id, @RequestBody PetDTO dto) {
        return service.updatePet(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletePet(@PathVariable Long id) {
        service.deletePet(id);
    }
}
