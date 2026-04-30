package com.pamperpaw.visit.service;

import com.pamperpaw.visit.dto.VisitRequestDTO;
import com.pamperpaw.visit.dto.VisitResponseDTO;
import com.pamperpaw.visit.dto.VetLeaveRequestDTO;
import com.pamperpaw.visit.dto.VetLeaveResponseDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface VisitService {

    VisitResponseDTO createVisit(VisitRequestDTO dto);

    List<VisitResponseDTO> getAllVisits();

    VisitResponseDTO getVisitById(Long id);

    CompletableFuture<List<VisitResponseDTO>> getAllVisitsAsync();

    void deleteVisit(Long id);

    // 🔥 ADD THIS (for frontend dashboard)
    List<VisitResponseDTO> getVisitsByCustomer(Long customerId);

    List<VisitResponseDTO> getVisitsByVet(Long vetId);

    List<VisitResponseDTO> getVisitsByPet(Long petId);

    List<VisitResponseDTO> getVisitsByVetAndDate(Long vetId, String date);

    List<String> getUnavailableSlots(Long vetId, String date);

    VetLeaveResponseDTO addVetLeave(VetLeaveRequestDTO dto);

    List<VetLeaveResponseDTO> getVetLeaves(Long vetId);

    VisitResponseDTO updateVisitStatus(Long id, String status);
}
