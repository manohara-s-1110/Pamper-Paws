package com.pamperpaw.visit.service.impl;

import com.pamperpaw.visit.dto.VisitRequestDTO;
import com.pamperpaw.visit.dto.VisitResponseDTO;
import com.pamperpaw.visit.dto.PaymentInitiateRequest;
import com.pamperpaw.visit.dto.PaymentInitiateResponse;
import com.pamperpaw.visit.dto.PaymentResponse;
import com.pamperpaw.visit.dto.PetDTO;
import com.pamperpaw.visit.dto.VetFeeResponse;
import com.pamperpaw.visit.entity.Visit;
import com.pamperpaw.visit.entity.PaymentMethod;
import com.pamperpaw.visit.entity.PaymentStatus;
import com.pamperpaw.visit.entity.VisitStatus;
import com.pamperpaw.visit.exception.ResourceNotFoundException;
import com.pamperpaw.visit.feign.CustomerClient;
import com.pamperpaw.visit.feign.PaymentClient;
import com.pamperpaw.visit.feign.VetClient;
import com.pamperpaw.visit.repository.VisitRepository;
import com.pamperpaw.visit.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final CustomerClient customerClient;
    private final VetClient vetClient;
    private final PaymentClient paymentClient;

    @Override
    public VisitResponseDTO createVisit(VisitRequestDTO dto) {
        if (dto.getPetId() == null) {
            throw new IllegalArgumentException("Pet ID is required");
        }
        PetDTO pet = validateReferences(dto.getCustomerId(), dto.getVetId(), dto.getPetId());
        validateSlotAvailable(dto.getVetId(), dto.getVisitDate(), dto.getTimeSlot());
        validateVetNotOnLeave(dto.getVetId(), dto.getVisitDate());
        BigDecimal consultationFee = fetchConsultationFee(dto.getVetId());

        Visit visit = new Visit();
        visit.setCustomerId(dto.getCustomerId());
        visit.setVetId(dto.getVetId());
        visit.setPetId(dto.getPetId());
        visit.setVisitDate(dto.getVisitDate());
        visit.setTimeSlot(dto.getTimeSlot()); 
        visit.setReason(dto.getReason());
        visit.setPaymentMethod(dto.getPaymentMethod());
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(PaymentMethod.CASH.equals(dto.getPaymentMethod()) ? VisitStatus.CONFIRMED : VisitStatus.PENDING);
        visit.setConsultationFee(consultationFee);

        Visit savedVisit = visitRepository.save(visit);
        PaymentInitiateResponse payment = paymentClient.initiate(PaymentInitiateRequest.builder()
                .appointmentId(savedVisit.getId())
                .userId(savedVisit.getCustomerId())
                .amount(consultationFee)
                .paymentMethod(savedVisit.getPaymentMethod())
                .build());
        log.info("Created visit with id={}", savedVisit.getId());
        VisitResponseDTO response = mapToResponse(savedVisit);
        response.setPetName(pet.getName());
        response.setPayment(payment);
        return response;
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
    @Transactional
    public void deleteVisit(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id: " + id));
        triggerRefundIfNeeded(visit);
        visitRepository.delete(visit);
        log.info("Deleted visit with id={}", id);
    }

    @Override
    @Transactional
    public void deleteVisitsByCustomer(Long customerId) {
        visitRepository.findByCustomerId(customerId).forEach(this::triggerRefundIfNeeded);
        visitRepository.deleteByCustomerId(customerId);
        log.info("Deleted visits for customerId={}", customerId);
    }

    @Override
    @Transactional
    public void deleteVisitsByPet(Long petId) {
        visitRepository.findByPetId(petId).forEach(this::triggerRefundIfNeeded);
        visitRepository.deleteByPetId(petId);
        log.info("Deleted visits for petId={}", petId);
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

    private PetDTO validateReferences(Long customerId, Long vetId, Long petId) {
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

        try {
            return customerClient.getPetById(petId);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Pet not found with id: " + petId);
        }
    }

    private BigDecimal fetchConsultationFee(Long vetId) {
        try {
            VetFeeResponse feeResponse = vetClient.getVetFee(vetId);
            if (feeResponse.getConsultationFee() == null || feeResponse.getConsultationFee().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResourceNotFoundException("Consultation fee is not configured for vet id: " + vetId);
            }
            return feeResponse.getConsultationFee();
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Unable to fetch consultation fee for vet id: " + vetId);
        }
    }

    private VisitResponseDTO mapToResponse(Visit visit) {
        reconcilePaymentStatus(visit);

        VisitResponseDTO response = new VisitResponseDTO();
        response.setId(visit.getId());
        response.setCustomerId(visit.getCustomerId());
        response.setVetId(visit.getVetId());
        response.setPetId(visit.getPetId());
        response.setPetName(resolvePetName(visit.getPetId()));
        response.setVisitDate(visit.getVisitDate());
        response.setReason(visit.getReason());
        response.setTimeSlot(visit.getTimeSlot());
        response.setStatus(visit.getStatus());
        response.setPaymentMethod(visit.getPaymentMethod());
        response.setPaymentStatus(visit.getPaymentStatus());
        response.setConsultationFee(visit.getConsultationFee());
        return response;
    }

    private void reconcilePaymentStatus(Visit visit) {
        if (!PaymentMethod.ONLINE.equals(visit.getPaymentMethod())
                || PaymentStatus.SUCCESS.equals(visit.getPaymentStatus())) {
            return;
        }

        try {
            PaymentResponse payment = paymentClient.getPayment(visit.getId());
            if (PaymentStatus.SUCCESS.equals(payment.getPaymentStatus())) {
                visit.setPaymentStatus(PaymentStatus.SUCCESS);
                if (VisitStatus.PENDING.equals(visit.getStatus())) {
                    visit.setStatus(VisitStatus.CONFIRMED);
                }
                visitRepository.save(visit);
            }
        } catch (Exception ex) {
            log.debug("Payment status reconciliation skipped for visit id={}", visit.getId());
        }
    }
    
    
    @Override
    public List<VisitResponseDTO> getVisitsByVetAndDate(Long vetId, String date) {

        return visitRepository.findByVetIdAndVisitDate(vetId, date)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<String> getUnavailableSlots(Long vetId, String date) {
        return visitRepository.findByVetIdAndVisitDate(vetId, date)
                .stream()
                .filter(visit -> !VisitStatus.MISSED.equals(visit.getStatus()))
                .filter(visit -> !VisitStatus.CANCELLED.equals(visit.getStatus()))
                .map(Visit::getTimeSlot)
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public VisitResponseDTO updateVisitPaymentStatus(Long id, PaymentStatus paymentStatus) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id: " + id));
        PaymentStatus previousPaymentStatus = visit.getPaymentStatus();
        visit.setPaymentStatus(paymentStatus);
        if (PaymentStatus.SUCCESS.equals(paymentStatus) && VisitStatus.PENDING.equals(visit.getStatus())) {
            visit.setStatus(VisitStatus.CONFIRMED);
        }
        if (PaymentStatus.FAILED.equals(paymentStatus) && PaymentStatus.SUCCESS.equals(previousPaymentStatus)) {
            visit.setPaymentStatus(previousPaymentStatus);
            triggerRefundIfNeeded(visit);
        }
        return mapToResponse(visitRepository.save(visit));
    }

    @Override
    @Transactional
    public VisitResponseDTO updateVisitStatus(Long id, VisitStatus status) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id: " + id));
        visit.setStatus(status);
        if (VisitStatus.CANCELLED.equals(status) || VisitStatus.MISSED.equals(status)) {
            triggerRefundIfNeeded(visit);
        }
        return mapToResponse(visitRepository.save(visit));
    }

    private String resolvePetName(Long petId) {
        if (petId == null) {
            return null;
        }
        try {
            return customerClient.getPetById(petId).getName();
        } catch (Exception ex) {
            log.debug("Unable to resolve pet name for petId={}", petId);
            return null;
        }
    }

    private void triggerRefundIfNeeded(Visit visit) {
        if (!PaymentMethod.ONLINE.equals(visit.getPaymentMethod())
                || !PaymentStatus.SUCCESS.equals(visit.getPaymentStatus())) {
            return;
        }

        try {
            PaymentResponse refund = paymentClient.refund(visit.getId());
            visit.setPaymentStatus(refund.getPaymentStatus());
        } catch (Exception ex) {
            log.warn("Refund trigger failed for visit id={}", visit.getId(), ex);
        }
    }

    private void validateSlotAvailable(Long vetId, String visitDate, String timeSlot) {
        if (visitRepository.existsByVetIdAndVisitDateAndTimeSlot(vetId, visitDate, timeSlot)) {
            throw new IllegalStateException("Slot already booked");
        }
    }

    private void validateVetNotOnLeave(Long vetId, String visitDate) {
        try {
            if (vetClient.getLeaveDates(vetId).contains(visitDate)) {
                throw new IllegalStateException("Veterinarian is on leave for this date");
            }
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Unable to check veterinarian leave for vet id: " + vetId);
        }
    }
}
