package com.pamperpaw.vet.controller;

import com.pamperpaw.vet.dto.VetDTO;
import com.pamperpaw.vet.service.VetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vets")
public class VetController {

    private final VetService vetService;

    @PostMapping
    public VetDTO createVet(@Valid @RequestBody VetDTO dto) {
        return vetService.createVet(dto);
    }

    @GetMapping
    public List<VetDTO> getAllVets() {
        return vetService.getAllVets();
    }

    @GetMapping("/{id}")
    public VetDTO getVetById(@PathVariable Long id) {
        return vetService.getVetById(id);
    }

    @GetMapping("/username/{username}")
    public VetDTO getVetByUsername(@PathVariable String username) {
        return vetService.getVetByUsername(username);
    }

    @PutMapping("/{id}")
    public VetDTO updateVet(@PathVariable Long id, @Valid @RequestBody VetDTO dto) {
        return vetService.updateVet(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVet(@PathVariable Long id) {
        vetService.deleteVet(id);
        return ResponseEntity.ok("Vet deleted successfully");
    }

    @GetMapping("/async")
    public CompletableFuture<List<VetDTO>> getAllVetsAsync() {
        return vetService.getAllVetsAsync();
    }

    @GetMapping("/specialization")
    public List<VetDTO> getBySpecialization(@RequestParam String specialization) {
        return vetService.getVetsBySpecialization(specialization);
    }

    @GetMapping("/location")
    public List<VetDTO> getByLocation(@RequestParam String location) {
        return vetService.getVetsByLocation(location);
    }

    @GetMapping("/experience")
    public List<VetDTO> getByExperience(@RequestParam int experience) {
        return vetService.getVetsByExperience(experience);
    }
    
    @GetMapping("/{id}/slots")
    public List<String> getSlots(@PathVariable Long id,
                                @RequestParam String date) {
        return vetService.getAvailableSlots(id, date);
    }
}
