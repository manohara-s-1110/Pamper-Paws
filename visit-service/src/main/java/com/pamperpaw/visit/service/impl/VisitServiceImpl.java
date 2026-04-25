package com.pamperpaw.visit.service.impl;

import com.pamperpaw.visit.dto.VisitRequestDTO;
import com.pamperpaw.visit.dto.VisitResponseDTO;
import com.pamperpaw.visit.entity.Visit;
import com.pamperpaw.visit.exception.ResourceNotFoundException;
import com.pamperpaw.visit.feign.CustomerClient;
import com.pamperpaw.visit.feign.VetClient;
import com.pamperpaw.visit.repository.VisitRepository;
import com.pamperpaw.visit.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final CustomerClient customerClient;
    private final VetClient vetClient;

    @Override
    public VisitResponseDTO createVisit(VisitRequestDTO dto) {
        validateReferences(dto.getCustomerId(), dto.getVetId());

        Visit visit = new Visit();
        visit.setCustomerId(dto.getCustomerId());
        visit.setVetId(dto.getVetId());
        visit.setPetId(dto.getPetId());
        visit.setVisitDate(dto.getVisitDate());
        visit.setTimeSlot(dto.getTimeSlot()); 
        visit.setReason(dto.getReason());

        Visit savedVisit = visitRepository.save(visit);
        log.info("Created visit with id={}", savedVisit.getId());
        return mapToResponse(savedVisit);
    }

    @Override
    public List<VisitResponseDTO> getAllVisits() {
        log.info("Fetching all visits");
        return visitRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public VisitResponseDTO getVisitById(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id: " + id));
        return mapToResponse(visit);
    }

    @Async
    @Override
    public CompletableFuture<List<VisitResponseDTO>> getAllVisitsAsync() {
        return CompletableFuture.completedFuture(getAllVisits());
    }

    @Override
    public void deleteVisit(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id: " + id));
        visitRepository.delete(visit);
        log.info("Deleted visit with id={}", id);
    }

    // 🔥 ADD THIS METHOD (for frontend dashboard)
    @Override
    public List<VisitResponseDTO> getVisitsByCustomer(Long customerId) {

        log.info("Fetching visits for customerId={}", customerId);

        return visitRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<VisitResponseDTO> getVisitsByVet(Long vetId) {
        log.info("Fetching visits for vetId={}", vetId);

        return visitRepository.findByVetId(vetId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<VisitResponseDTO> getVisitsByPet(Long petId) {
        log.info("Fetching visits for petId={}", petId);

        return visitRepository.findByPetId(petId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateReferences(Long customerId, Long vetId) {
        try {
            customerClient.getCustomerById(customerId);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        try {
            vetClient.getVetById(vetId);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Vet not found with id: " + vetId);
        }
    }

    private VisitResponseDTO mapToResponse(Visit visit) {
        VisitResponseDTO response = new VisitResponseDTO();
        response.setId(visit.getId());
        response.setCustomerId(visit.getCustomerId());
        response.setVetId(visit.getVetId());
        response.setPetId(visit.getPetId());
        response.setVisitDate(visit.getVisitDate());
        response.setReason(visit.getReason());
        response.setTimeSlot(visit.getTimeSlot());
        return response;
    }
    
    
    @Override
    public List<VisitResponseDTO> getVisitsByVetAndDate(Long vetId, String date) {

        return visitRepository.findByVetIdAndVisitDate(vetId, date)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}
