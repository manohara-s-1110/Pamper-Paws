package com.pamperpaw.visit.service.impl;

import com.pamperpaw.visit.dto.VisitRequestDTO;
import com.pamperpaw.visit.dto.VisitResponseDTO;
import com.pamperpaw.visit.dto.PaymentInitiateResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceImplTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private VetClient vetClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private VisitServiceImpl visitService;

    @Test
    void createVisitValidatesReferencesAndSaves() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-04-07");
        request.setTimeSlot("10 AM - 11 AM");
        request.setReason("Checkup");
        request.setPaymentMethod(PaymentMethod.ONLINE);

        Visit savedVisit = new Visit();
        savedVisit.setId(9L);
        savedVisit.setCustomerId(1L);
        savedVisit.setVetId(2L);
        savedVisit.setPetId(3L);
        savedVisit.setVisitDate("2026-04-07");
        savedVisit.setTimeSlot("10 AM - 11 AM");
        savedVisit.setReason("Checkup");
        savedVisit.setStatus(VisitStatus.PENDING);
        savedVisit.setPaymentMethod(PaymentMethod.ONLINE);
        savedVisit.setPaymentStatus(PaymentStatus.PENDING);
        savedVisit.setConsultationFee(BigDecimal.valueOf(500));

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenReturn(new Object());
        when(vetClient.getVetFee(2L)).thenReturn(new VetFeeResponse(2L, BigDecimal.valueOf(500)));
        when(visitRepository.save(any(Visit.class))).thenReturn(savedVisit);
        when(paymentClient.initiate(any())).thenReturn(new PaymentInitiateResponse());

        VisitResponseDTO response = visitService.createVisit(request);

        assertEquals(9L, response.getId());
        assertEquals("Checkup", response.getReason());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
    }

    @Test
    void createVisitThrowsWhenCustomerMissing() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-04-07");
        request.setTimeSlot("10 AM - 11 AM");
        request.setReason("Checkup");

        when(customerClient.getCustomerById(1L)).thenThrow(new RuntimeException("missing"));

        assertThrows(ResourceNotFoundException.class, () -> visitService.createVisit(request));
    }

    @Test
    void createVisitThrowsWhenVetMissing() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-04-07");
        request.setTimeSlot("10 AM - 11 AM");
        request.setReason("Checkup");

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenThrow(new RuntimeException("missing"));

        assertThrows(ResourceNotFoundException.class, () -> visitService.createVisit(request));
    }

    @Test
    void getVisitByIdThrowsWhenMissing() {
        when(visitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> visitService.getVisitById(99L));
    }

    @Test
    void getVisitByIdReturnsMappedVisit() {
        Visit visit = new Visit();
        visit.setId(2L);
        visit.setCustomerId(1L);
        visit.setVetId(3L);
        visit.setPetId(5L);
        visit.setVisitDate("2026-04-07");
        visit.setTimeSlot("11 AM - 12 PM");
        visit.setReason("Vaccination");
        visit.setStatus(VisitStatus.CONFIRMED);
        visit.setPaymentMethod(PaymentMethod.CASH);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setConsultationFee(BigDecimal.valueOf(500));

        when(visitRepository.findById(2L)).thenReturn(Optional.of(visit));

        VisitResponseDTO response = visitService.getVisitById(2L);

        assertEquals(3L, response.getVetId());
        assertEquals("Vaccination", response.getReason());
    }

    @Test
    void getAllVisitsMapsEntities() {
        Visit visit = new Visit();
        visit.setId(1L);
        visit.setCustomerId(1L);
        visit.setVetId(2L);
        visit.setPetId(3L);
        visit.setVisitDate("2026-04-07");
        visit.setTimeSlot("10 AM - 11 AM");
        visit.setReason("Checkup");
        visit.setStatus(VisitStatus.PENDING);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setConsultationFee(BigDecimal.valueOf(500));

        when(visitRepository.findAll()).thenReturn(List.of(visit));

        List<VisitResponseDTO> response = visitService.getAllVisits();

        assertEquals(1, response.size());
        assertEquals(2L, response.get(0).getVetId());
    }

    @Test
    void getAllVisitsAsyncDelegatesToAllVisits() throws ExecutionException, InterruptedException {
        Visit visit = new Visit();
        visit.setId(3L);
        visit.setCustomerId(1L);
        visit.setVetId(2L);
        visit.setPetId(3L);
        visit.setVisitDate("2026-04-07");
        visit.setTimeSlot("10 AM - 11 AM");
        visit.setReason("Checkup");
        visit.setStatus(VisitStatus.PENDING);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setConsultationFee(BigDecimal.valueOf(500));

        when(visitRepository.findAll()).thenReturn(List.of(visit));

        List<VisitResponseDTO> response = visitService.getAllVisitsAsync().get();

        assertEquals(1, response.size());
        assertEquals(3L, response.get(0).getId());
    }

    @Test
    void deleteVisitDeletesExistingRecord() {
        Visit visit = new Visit();
        visit.setId(4L);

        when(visitRepository.findById(4L)).thenReturn(Optional.of(visit));

        visitService.deleteVisit(4L);

        verify(visitRepository).delete(visit);
    }

    @Test
    void deleteVisitThrowsWhenMissing() {
        when(visitRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> visitService.deleteVisit(4L));
    }

    @Test
    void updateVisitPaymentStatusConfirmsOnSuccess() {
        Visit visit = new Visit();
        visit.setId(8L);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(VisitStatus.PENDING);

        when(visitRepository.findById(8L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);

        VisitResponseDTO response = visitService.updateVisitPaymentStatus(8L, PaymentStatus.SUCCESS);

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals(VisitStatus.CONFIRMED, response.getStatus());
    }
}
