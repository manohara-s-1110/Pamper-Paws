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
    void createVisitThrowsWhenPetMissing() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-04-07");
        request.setTimeSlot("10 AM - 11 AM");

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenReturn(new Object());
        when(customerClient.getPetById(3L)).thenThrow(new RuntimeException("missing"));

        assertThrows(ResourceNotFoundException.class, () -> visitService.createVisit(request));
    }

    @Test
    void createVisitThrowsWhenPetIdMissing() {
        VisitRequestDTO request = new VisitRequestDTO();

        assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(request));
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
    void getVisitByIdReconcilesSuccessfulOnlinePayment() {
        Visit visit = futureVisit();
        visit.setId(22L);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(VisitStatus.PENDING);

        com.pamperpaw.visit.dto.PaymentResponse payment = new com.pamperpaw.visit.dto.PaymentResponse();
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        when(visitRepository.findById(22L)).thenReturn(Optional.of(visit));
        when(paymentClient.getPayment(22L)).thenReturn(payment);
        when(visitRepository.save(visit)).thenReturn(visit);

        VisitResponseDTO response = visitService.getVisitById(22L);

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals(VisitStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void getVisitByIdLeavesPendingPaymentUnchangedWhenPaymentIsNotSuccessful() {
        Visit visit = futureVisit();
        visit.setId(29L);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(VisitStatus.PENDING);

        com.pamperpaw.visit.dto.PaymentResponse payment = new com.pamperpaw.visit.dto.PaymentResponse();
        payment.setPaymentStatus(PaymentStatus.FAILED);

        when(visitRepository.findById(29L)).thenReturn(Optional.of(visit));
        when(paymentClient.getPayment(29L)).thenReturn(payment);

        VisitResponseDTO response = visitService.getVisitById(29L);

        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        assertEquals(VisitStatus.PENDING, response.getStatus());
        verify(visitRepository, never()).save(visit);
    }

    @Test
    void getVisitByIdReconcilesPaymentWithoutChangingNonPendingStatus() {
        Visit visit = futureVisit();
        visit.setId(30L);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(VisitStatus.COMPLETED);

        com.pamperpaw.visit.dto.PaymentResponse payment = new com.pamperpaw.visit.dto.PaymentResponse();
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        when(visitRepository.findById(30L)).thenReturn(Optional.of(visit));
        when(paymentClient.getPayment(30L)).thenReturn(payment);
        when(visitRepository.save(visit)).thenReturn(visit);

        VisitResponseDTO response = visitService.getVisitById(30L);

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals(VisitStatus.COMPLETED, response.getStatus());
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
    void groupedLookupMethodsMapRepositoryResults() {
        Visit visit = futureVisit();
        visit.setStatus(VisitStatus.CONFIRMED);
        visit.setPaymentMethod(PaymentMethod.CASH);
        visit.setPaymentStatus(PaymentStatus.PENDING);

        when(visitRepository.findByCustomerId(1L)).thenReturn(List.of(visit));
        when(visitRepository.findByVetId(2L)).thenReturn(List.of(visit));
        when(visitRepository.findByPetId(3L)).thenReturn(List.of(visit));
        when(visitRepository.findByVetIdAndVisitDate(2L, "2099-05-08")).thenReturn(List.of(visit));

        assertEquals(1, visitService.getVisitsByCustomer(1L).size());
        assertEquals(1, visitService.getVisitsByVet(2L).size());
        assertEquals(1, visitService.getVisitsByPet(3L).size());
        assertEquals(1, visitService.getVisitsByVetAndDate(2L, "2099-05-08").size());
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

    @Test
    void updateVisitPaymentStatusThrowsWhenVisitMissing() {
        when(visitRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> visitService.updateVisitPaymentStatus(404L, PaymentStatus.SUCCESS));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void updateVisitPaymentStatusDoesNotConfirmWhenPaymentFailed() {
        Visit visit = futureVisit();
        visit.setId(27L);
        visit.setPaymentMethod(PaymentMethod.CASH);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(VisitStatus.PENDING);

        when(visitRepository.findById(27L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);

        VisitResponseDTO response = visitService.updateVisitPaymentStatus(27L, PaymentStatus.FAILED);

        assertEquals(PaymentStatus.FAILED, response.getPaymentStatus());
        assertEquals(VisitStatus.PENDING, response.getStatus());
    }

    @Test
    void updateVisitPaymentStatusSuccessDoesNotOverwriteCompletedStatus() {
        Visit visit = futureVisit();
        visit.setId(31L);
        visit.setPaymentMethod(PaymentMethod.CASH);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(VisitStatus.COMPLETED);

        when(visitRepository.findById(31L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);

        VisitResponseDTO response = visitService.updateVisitPaymentStatus(31L, PaymentStatus.SUCCESS);

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals(VisitStatus.COMPLETED, response.getStatus());
    }

    @Test
    void updateVisitPaymentStatusCanMarkSuccessfulPaymentFailedDirectly() {
        Visit visit = futureVisit();
        visit.setId(18L);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.SUCCESS);
        visit.setStatus(VisitStatus.CONFIRMED);

        when(visitRepository.findById(18L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);

        VisitResponseDTO response = visitService.updateVisitPaymentStatus(18L, PaymentStatus.FAILED);

        assertEquals(PaymentStatus.FAILED, response.getPaymentStatus());
        verify(paymentClient).getPayment(18L);
    }

    @Test
    void updateVisitStatusUpdatesNonCancellationStatus() {
        Visit visit = futureVisit();
        visit.setId(19L);
        visit.setPaymentMethod(PaymentMethod.CASH);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(VisitStatus.CONFIRMED);

        when(visitRepository.findById(19L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);

        assertEquals(VisitStatus.COMPLETED, visitService.updateVisitStatus(19L, VisitStatus.COMPLETED).getStatus());
    }

    @Test
    void updateVisitStatusThrowsWhenVisitMissing() {
        when(visitRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> visitService.updateVisitStatus(404L, VisitStatus.COMPLETED));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void cancelVisitCancelsSuccessfulOnlinePaymentWithoutPaymentClientCall() {
        Visit visit = futureVisit();
        visit.setId(11L);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.SUCCESS);
        visit.setStatus(VisitStatus.CONFIRMED);

        when(visitRepository.findById(11L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);

        VisitResponseDTO response = visitService.cancelVisit(11L);

        assertEquals(VisitStatus.CANCELLED, response.getStatus());
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        verifyNoInteractions(paymentClient);
    }

    @Test
    void cancelVisitCancelsCashAppointment() {
        Visit visit = futureVisit();
        visit.setId(12L);
        visit.setPaymentMethod(PaymentMethod.CASH);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(VisitStatus.CONFIRMED);

        when(visitRepository.findById(12L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);

        VisitResponseDTO response = visitService.cancelVisit(12L);

        assertEquals(VisitStatus.CANCELLED, response.getStatus());
        verifyNoInteractions(paymentClient);
    }

    @Test
    void cancelVisitThrowsWhenMissing() {
        when(visitRepository.findById(13L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> visitService.cancelVisit(13L));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void updateVisitStatusMarksMissedWithoutPaymentClientCall() {
        Visit visit = futureVisit();
        visit.setId(16L);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.SUCCESS);
        visit.setStatus(VisitStatus.CONFIRMED);

        when(visitRepository.findById(16L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);

        VisitResponseDTO response = visitService.updateVisitStatus(16L, VisitStatus.MISSED);

        assertEquals(VisitStatus.MISSED, response.getStatus());
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        verifyNoInteractions(paymentClient);
    }

    @Test
    void cancelVisitRejectsAlreadyCancelledAppointment() {
        Visit visit = futureVisit();
        visit.setStatus(VisitStatus.CANCELLED);

        when(visitRepository.findById(14L)).thenReturn(Optional.of(visit));

        assertThrows(IllegalStateException.class, () -> visitService.cancelVisit(14L));
    }

    @Test
    void updateVisitStatusDelegatesCancelledStatusToCancelFlow() {
        Visit visit = futureVisit();
        visit.setId(15L);
        visit.setPaymentMethod(PaymentMethod.CASH);
        visit.setPaymentStatus(PaymentStatus.PENDING);
        visit.setStatus(VisitStatus.CONFIRMED);

        when(visitRepository.findById(15L)).thenReturn(Optional.of(visit));
        when(visitRepository.save(visit)).thenReturn(visit);

        assertEquals(VisitStatus.CANCELLED, visitService.updateVisitStatus(15L, VisitStatus.CANCELLED).getStatus());
    }

    @Test
    void getUnavailableSlotsExcludesMissedAndCancelledVisits() {
        Visit confirmed = futureVisit();
        confirmed.setTimeSlot("10 AM - 11 AM");
        confirmed.setStatus(VisitStatus.CONFIRMED);

        Visit cancelled = futureVisit();
        cancelled.setTimeSlot("11 AM - 12 PM");
        cancelled.setStatus(VisitStatus.CANCELLED);

        Visit missed = futureVisit();
        missed.setTimeSlot("12 PM - 1 PM");
        missed.setStatus(VisitStatus.MISSED);

        when(visitRepository.findByVetIdAndVisitDate(2L, "2026-05-08"))
                .thenReturn(List.of(confirmed, cancelled, missed));

        assertEquals(List.of("10 AM - 11 AM"), visitService.getUnavailableSlots(2L, "2026-05-08"));
    }

    @Test
    void deleteVisitsByCustomerDeletesPaidOnlineVisitsDirectly() {
        Visit visit = futureVisit();
        visit.setId(17L);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.SUCCESS);

        visitService.deleteVisitsByCustomer(1L);

        verify(paymentClient).deletePaymentsByUser(1L);
        verify(visitRepository).deleteByCustomerId(1L);
    }

    @Test
    void deleteVisitsByCustomerThrowsWhenPaymentDeleteFails() {
        doThrow(new RuntimeException("payment service down")).when(paymentClient).deletePaymentsByUser(1L);

        assertThrows(IllegalStateException.class, () -> visitService.deleteVisitsByCustomer(1L));
        verify(visitRepository, never()).deleteByCustomerId(any());
    }

    @Test
    void deleteVisitsByPetDeletesPaymentsForExistingVisits() {
        Visit visit = futureVisit();
        visit.setId(20L);
        visit.setPaymentMethod(PaymentMethod.CASH);
        visit.setPaymentStatus(PaymentStatus.PENDING);

        when(visitRepository.findByPetId(3L)).thenReturn(List.of(visit));

        visitService.deleteVisitsByPet(3L);

        verify(paymentClient).deletePayment(20L);
        verify(visitRepository).deleteByPetId(3L);
    }

    @Test
    void createVisitRejectsBookedSlot() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-05-08");
        request.setTimeSlot("10 AM - 11 AM");

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenReturn(new Object());
        when(customerClient.getPetById(3L)).thenReturn(new com.pamperpaw.visit.dto.PetDTO());
        when(visitRepository.existsByVetIdAndVisitDateAndTimeSlot(2L, "2026-05-08", "10 AM - 11 AM")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> visitService.createVisit(request));
    }

    @Test
    void createVisitRejectsVetLeaveDay() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-05-08");
        request.setTimeSlot("10 AM - 11 AM");

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenReturn(new Object());
        when(customerClient.getPetById(3L)).thenReturn(new com.pamperpaw.visit.dto.PetDTO());
        when(vetClient.getLeaveDates(2L)).thenReturn(List.of("2026-05-08"));

        assertThrows(IllegalStateException.class, () -> visitService.createVisit(request));
    }

    @Test
    void createVisitRejectsMissingConsultationFee() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-05-08");
        request.setTimeSlot("10 AM - 11 AM");
        request.setPaymentMethod(PaymentMethod.ONLINE);

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenReturn(new Object());
        when(customerClient.getPetById(3L)).thenReturn(new com.pamperpaw.visit.dto.PetDTO());
        when(vetClient.getLeaveDates(2L)).thenReturn(List.of());
        when(vetClient.getVetFee(2L)).thenReturn(new VetFeeResponse(2L, BigDecimal.ZERO));

        assertThrows(ResourceNotFoundException.class, () -> visitService.createVisit(request));
    }

    @Test
    void createVisitRejectsNullConsultationFee() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-05-08");
        request.setTimeSlot("10 AM - 11 AM");
        request.setPaymentMethod(PaymentMethod.ONLINE);

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenReturn(new Object());
        when(customerClient.getPetById(3L)).thenReturn(new com.pamperpaw.visit.dto.PetDTO());
        when(vetClient.getLeaveDates(2L)).thenReturn(List.of());
        when(vetClient.getVetFee(2L)).thenReturn(new VetFeeResponse(2L, null));

        assertThrows(ResourceNotFoundException.class, () -> visitService.createVisit(request));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void createVisitThrowsWhenFeeLookupFails() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-05-08");
        request.setTimeSlot("10 AM - 11 AM");
        request.setPaymentMethod(PaymentMethod.ONLINE);

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenReturn(new Object());
        when(customerClient.getPetById(3L)).thenReturn(new com.pamperpaw.visit.dto.PetDTO());
        when(vetClient.getLeaveDates(2L)).thenReturn(List.of());
        when(vetClient.getVetFee(2L)).thenThrow(new RuntimeException("fee service down"));

        assertThrows(ResourceNotFoundException.class, () -> visitService.createVisit(request));
        verify(visitRepository, never()).save(any());
    }

    @Test
    void createVisitThrowsWhenLeaveCheckFails() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-05-08");
        request.setTimeSlot("10 AM - 11 AM");

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenReturn(new Object());
        when(customerClient.getPetById(3L)).thenReturn(new com.pamperpaw.visit.dto.PetDTO());
        when(vetClient.getLeaveDates(2L)).thenThrow(new RuntimeException("down"));

        assertThrows(ResourceNotFoundException.class, () -> visitService.createVisit(request));
    }

    @Test
    void cancelVisitRejectsCompletedAppointment() {
        Visit visit = futureVisit();
        visit.setStatus(VisitStatus.COMPLETED);

        when(visitRepository.findById(23L)).thenReturn(Optional.of(visit));

        assertThrows(IllegalStateException.class, () -> visitService.cancelVisit(23L));
    }

    @Test
    void cancelVisitRejectsMissedAppointment() {
        Visit visit = futureVisit();
        visit.setStatus(VisitStatus.MISSED);

        when(visitRepository.findById(32L)).thenReturn(Optional.of(visit));

        assertThrows(IllegalStateException.class, () -> visitService.cancelVisit(32L));
    }

    @Test
    void cancelVisitRejectsPastAppointment() {
        Visit visit = futureVisit();
        visit.setVisitDate("2000-01-01");
        visit.setStatus(VisitStatus.CONFIRMED);

        when(visitRepository.findById(24L)).thenReturn(Optional.of(visit));

        assertThrows(IllegalStateException.class, () -> visitService.cancelVisit(24L));
    }

    @Test
    void cancelVisitRejectsUnsupportedSlotFormat() {
        Visit visit = futureVisit();
        visit.setTimeSlot("morning");
        visit.setStatus(VisitStatus.CONFIRMED);

        when(visitRepository.findById(25L)).thenReturn(Optional.of(visit));

        assertThrows(IllegalStateException.class, () -> visitService.cancelVisit(25L));
    }

    @Test
    void deleteVisitDeletesSuccessfulOnlineVisitDirectly() {
        Visit visit = futureVisit();
        visit.setId(26L);
        visit.setPaymentMethod(PaymentMethod.ONLINE);
        visit.setPaymentStatus(PaymentStatus.SUCCESS);

        when(visitRepository.findById(26L)).thenReturn(Optional.of(visit));

        visitService.deleteVisit(26L);

        verify(paymentClient).deletePayment(26L);
        verify(visitRepository).delete(visit);
    }

    @Test
    void deleteVisitStillDeletesVisitWhenPaymentDeleteFails() {
        Visit visit = futureVisit();
        visit.setId(28L);

        when(visitRepository.findById(28L)).thenReturn(Optional.of(visit));
        doThrow(new RuntimeException("payment missing")).when(paymentClient).deletePayment(28L);

        visitService.deleteVisit(28L);

        verify(visitRepository).delete(visit);
    }

    @Test
    void createCashVisitIsConfirmedImmediately() {
        VisitRequestDTO request = new VisitRequestDTO();
        request.setCustomerId(1L);
        request.setVetId(2L);
        request.setPetId(3L);
        request.setVisitDate("2026-05-08");
        request.setTimeSlot("10 AM - 11 AM");
        request.setReason("Checkup");
        request.setPaymentMethod(PaymentMethod.CASH);

        com.pamperpaw.visit.dto.PetDTO pet = new com.pamperpaw.visit.dto.PetDTO();
        pet.setName("Max");

        Visit saved = futureVisit();
        saved.setId(21L);
        saved.setStatus(VisitStatus.CONFIRMED);
        saved.setPaymentMethod(PaymentMethod.CASH);
        saved.setPaymentStatus(PaymentStatus.PENDING);

        when(customerClient.getCustomerById(1L)).thenReturn(new Object());
        when(vetClient.getVetById(2L)).thenReturn(new Object());
        when(customerClient.getPetById(3L)).thenReturn(pet);
        when(vetClient.getLeaveDates(2L)).thenReturn(List.of());
        when(vetClient.getVetFee(2L)).thenReturn(new VetFeeResponse(2L, BigDecimal.valueOf(500)));
        when(visitRepository.save(any(Visit.class))).thenReturn(saved);
        when(paymentClient.initiate(any())).thenReturn(new PaymentInitiateResponse());

        assertEquals(VisitStatus.CONFIRMED, visitService.createVisit(request).getStatus());
    }

    private Visit futureVisit() {
        Visit visit = new Visit();
        visit.setId(1L);
        visit.setCustomerId(1L);
        visit.setVetId(2L);
        visit.setPetId(3L);
        visit.setVisitDate("2099-05-08");
        visit.setTimeSlot("10 AM - 11 AM");
        visit.setReason("Checkup");
        visit.setConsultationFee(BigDecimal.valueOf(500));
        return visit;
    }
}
