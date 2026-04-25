package com.pamperpaw.vet.service;

import com.pamperpaw.vet.dto.VetDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface VetService {

    VetDTO createVet(VetDTO dto);

    List<VetDTO> getAllVets();

    VetDTO getVetById(Long id);

    VetDTO getVetByUsername(String username);

    VetDTO updateVet(Long id, VetDTO dto);

    void deleteVet(Long id);

    CompletableFuture<List<VetDTO>> getAllVetsAsync();

    List<VetDTO> getVetsBySpecialization(String specialization);

    List<VetDTO> getVetsByLocation(String location);

    List<VetDTO> getVetsByExperience(int experience);
    List<String> getAvailableSlots(Long vetId, String date);
}
