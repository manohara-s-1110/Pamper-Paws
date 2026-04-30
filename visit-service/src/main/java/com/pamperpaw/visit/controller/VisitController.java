package com.pamperpaw.visit.controller;

import com.pamperpaw.visit.dto.VisitRequestDTO;
import com.pamperpaw.visit.dto.VisitResponseDTO;
import com.pamperpaw.visit.dto.VetLeaveRequestDTO;
import com.pamperpaw.visit.dto.VetLeaveResponseDTO;
import com.pamperpaw.visit.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/visit")
public class VisitController {

    private final VisitService service;

    @PostMapping
    public VisitResponseDTO createVisit(@Valid @RequestBody VisitRequestDTO dto) {
        return service.createVisit(dto);
    }

    @GetMapping
    public List<VisitResponseDTO> getAllVisits() {
        return service.getAllVisits();
    }

    @GetMapping("/{id}")
    public VisitResponseDTO getVisitById(@PathVariable Long id) {
        return service.getVisitById(id);
    }

    @GetMapping("/async")
    public CompletableFuture<List<VisitResponseDTO>> getAllVisitsAsync() {
        return service.getAllVisitsAsync();
    }

    @DeleteMapping("/{id}")
    public void deleteVisit(@PathVariable Long id) {
        service.deleteVisit(id);
    }

    // 🔥 ADD THIS (for dashboard appointments)
    @GetMapping("/customer/{customerId}")
    public List<VisitResponseDTO> getVisitsByCustomer(@PathVariable Long customerId) {
        return service.getVisitsByCustomer(customerId);
    }

    @GetMapping("/vet/{vetId}")
    public List<VisitResponseDTO> getVisitsByVet(@PathVariable Long vetId) {
        return service.getVisitsByVet(vetId);
    }

    @GetMapping("/pet/{petId}")
    public List<VisitResponseDTO> getVisitsByPet(@PathVariable Long petId) {
        return service.getVisitsByPet(petId);
    }

    @GetMapping(value = "/vet/{vetId}", params = "date")
    public List<VisitResponseDTO> getVisitsByVetAndDate(
            @PathVariable Long vetId,
            @RequestParam String date) {

        return service.getVisitsByVetAndDate(vetId, date);
    }

    @GetMapping("/vet/{vetId}/unavailable-slots")
    public List<String> getUnavailableSlots(@PathVariable Long vetId, @RequestParam String date) {
        return service.getUnavailableSlots(vetId, date);
    }

    @PostMapping("/vet/{vetId}/leaves")
    public VetLeaveResponseDTO addVetLeave(@PathVariable Long vetId, @Valid @RequestBody VetLeaveRequestDTO dto) {
        dto.setVetId(vetId);
        return service.addVetLeave(dto);
    }

    @GetMapping("/vet/{vetId}/leaves")
    public List<VetLeaveResponseDTO> getVetLeaves(@PathVariable Long vetId) {
        return service.getVetLeaves(vetId);
    }

    @PatchMapping("/{id}/status")
    public VisitResponseDTO updateVisitStatus(@PathVariable Long id, @RequestParam String status) {
        return service.updateVisitStatus(id, status);
    }
}
