package com.pamperpaw.visit.service;

import com.pamperpaw.visit.dto.VisitRequestDTO;
import com.pamperpaw.visit.dto.VisitResponseDTO;
import com.pamperpaw.visit.entity.PaymentStatus;
import com.pamperpaw.visit.entity.VisitStatus;

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

    VisitResponseDTO updateVisitPaymentStatus(Long id, PaymentStatus paymentStatus);

    VisitResponseDTO updateVisitStatus(Long id, VisitStatus status);
}
