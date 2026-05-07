package com.pamperpaw.vet.service.impl;

import com.pamperpaw.vet.dto.VetDTO;
import com.pamperpaw.vet.entity.VetLeave;
import com.pamperpaw.vet.entity.Vet;
import com.pamperpaw.vet.exception.VetNotFoundException;
import com.pamperpaw.vet.repository.VetLeaveRepository;
import com.pamperpaw.vet.repository.VetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VetServiceImplTest {

    @Mock
    private VetRepository vetRepository;

    @Mock
    private VetLeaveRepository vetLeaveRepository;

    @InjectMocks
    private VetServiceImpl vetService;

    @Test
    void createVetPersistsAndMapsResponse() {
        VetDTO dto = VetDTO.builder()
                .name("Dr Paws")
                .username("drpaws")
                .specialization("Surgery")
                .experience(5)
                .phone("9876543210")
                .email("paws@test.com")
                .clinicAddress("Chennai")
                .availableDays("Mon")
                .availableTime("10AM")
                .consultationFee(BigDecimal.valueOf(600))
                .build();

        Vet savedVet = Vet.builder()
                .id(1L)
                .name(dto.getName())
                .username(dto.getUsername())
                .specialization(dto.getSpecialization())
                .experience(dto.getExperience())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .clinicAddress(dto.getClinicAddress())
                .availableDays(dto.getAvailableDays())
                .availableTime(dto.getAvailableTime())
                .consultationFee(dto.getConsultationFee())
                .build();

        when(vetRepository.save(any(Vet.class))).thenReturn(savedVet);

        VetDTO response = vetService.createVet(dto);

        assertEquals(1L, response.getId());
        assertEquals("Dr Paws", response.getName());
        assertEquals(BigDecimal.valueOf(600), response.getConsultationFee());
    }

    @Test
    void createVetRejectsDuplicateUsername() {
        VetDTO dto = VetDTO.builder()
                .username("drpaws")
                .email("paws@test.com")
                .build();

        when(vetRepository.existsByUsername("drpaws")).thenReturn(true);

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> vetService.createVet(dto));
    }

    @Test
    void createVetRejectsDuplicateEmail() {
        VetDTO dto = VetDTO.builder()
                .username("drpaws")
                .name("Dr Paws")
                .email("paws@test.com")
                .phone("9876543210")
                .specialization("Surgery")
                .clinicAddress("Chennai")
                .availableDays("Mon")
                .availableTime("10 AM - 12 PM")
                .build();

        when(vetRepository.findByEmail("paws@test.com")).thenReturn(Optional.of(Vet.builder().id(2L).build()));

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> vetService.createVet(dto));
    }

    @Test
    void getVetByIdThrowsWhenMissing() {
        when(vetRepository.findById(11L)).thenReturn(Optional.empty());

        assertThrows(VetNotFoundException.class, () -> vetService.getVetById(11L));
    }

    @Test
    void getAllVetsAsyncWrapsMappedList() throws Exception {
        Vet vet = Vet.builder()
                .id(1L)
                .name("Dr Paws")
                .username("drpaws")
                .specialization("Surgery")
                .experience(5)
                .phone("9876543210")
                .email("paws@test.com")
                .clinicAddress("Chennai")
                .availableDays("Mon")
                .availableTime("10AM")
                .consultationFee(BigDecimal.valueOf(500))
                .build();

        when(vetRepository.findAll()).thenReturn(List.of(vet));

        CompletableFuture<List<VetDTO>> future = vetService.getAllVetsAsync();

        assertEquals(1, future.get().size());
        assertEquals("Dr Paws", future.get().get(0).getName());
    }

    @Test
    void getVetByIdReturnsMappedVet() {
        Vet vet = Vet.builder()
                .id(1L)
                .name("Dr Paws")
                .username("drpaws")
                .specialization("Surgery")
                .experience(5)
                .phone("9876543210")
                .email("paws@test.com")
                .clinicAddress("Chennai")
                .availableDays("Mon")
                .availableTime("10AM")
                .consultationFee(BigDecimal.valueOf(500))
                .build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(vet));

        VetDTO response = vetService.getVetById(1L);

        assertEquals("Dr Paws", response.getName());
        assertEquals("Chennai", response.getClinicAddress());
    }

    @Test
    void getVetByUsernameReturnsMappedVet() {
        Vet vet = Vet.builder()
                .id(1L)
                .username("drpaws")
                .name("Dr Paws")
                .specialization("Surgery")
                .clinicAddress("Chennai")
                .availableDays("Mon")
                .availableTime("10 AM - 12 PM")
                .build();

        when(vetRepository.findByUsername("drpaws")).thenReturn(Optional.of(vet));

        assertEquals("Dr Paws", vetService.getVetByUsername("drpaws").getName());
    }

    @Test
    void getVetByUsernameThrowsWhenMissing() {
        when(vetRepository.findByUsername("none")).thenReturn(Optional.empty());

        assertThrows(VetNotFoundException.class, () -> vetService.getVetByUsername("none"));
    }

    @Test
    void updateVetUpdatesExistingVet() {
        Vet existing = Vet.builder().id(1L).username("drpaws").build();
        Vet saved = Vet.builder()
                .id(1L)
                .username("drpaws")
                .name("Updated")
                .specialization("Dermatology")
                .experience(8)
                .phone("9999999999")
                .email("updated@test.com")
                .clinicAddress("Madurai")
                .availableDays("Tue")
                .availableTime("11AM")
                .consultationFee(BigDecimal.valueOf(700))
                .build();

        VetDTO dto = VetDTO.builder()
                .name("Updated")
                .username("drpaws")
                .specialization("Dermatology")
                .experience(8)
                .phone("9999999999")
                .email("updated@test.com")
                .clinicAddress("Madurai")
                .availableDays("Tue")
                .availableTime("11AM")
                .consultationFee(BigDecimal.valueOf(700))
                .build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vetRepository.save(existing)).thenReturn(saved);

        VetDTO response = vetService.updateVet(1L, dto);

        assertEquals("Updated", response.getName());
        assertEquals("Dermatology", response.getSpecialization());
        assertEquals(BigDecimal.valueOf(700), response.getConsultationFee());
    }

    @Test
    void updateVetRejectsUsernameChange() {
        Vet existing = Vet.builder().id(1L).username("drpaws").build();
        VetDTO dto = VetDTO.builder().username("other").build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> vetService.updateVet(1L, dto));
    }

    @Test
    void updateVetThrowsWhenMissing() {
        when(vetRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(VetNotFoundException.class, () -> vetService.updateVet(2L, VetDTO.builder().build()));
    }

    @Test
    void deleteVetDeletesExistingRecord() {
        when(vetRepository.findById(4L)).thenReturn(Optional.of(Vet.builder().id(4L).build()));

        vetService.deleteVet(4L);

        verify(vetRepository).deleteById(4L);
    }

    @Test
    void deleteVetThrowsWhenMissing() {
        when(vetRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(VetNotFoundException.class, () -> vetService.deleteVet(4L));
    }

    @Test
    void filterQueriesMapRepositoryResults() {
        Vet vet = Vet.builder()
                .id(1L)
                .name("Dr Paws")
                .username("drpaws")
                .specialization("Surgery")
                .experience(5)
                .clinicAddress("Chennai")
                .consultationFee(BigDecimal.valueOf(500))
                .build();

        when(vetRepository.findBySpecialization("Surgery")).thenReturn(List.of(vet));
        when(vetRepository.findByClinicAddress("Chennai")).thenReturn(List.of(vet));
        when(vetRepository.findByExperienceGreaterThanEqual(5)).thenReturn(List.of(vet));

        assertEquals(1, vetService.getVetsBySpecialization("Surgery").size());
        assertEquals(1, vetService.getVetsByLocation("Chennai").size());
        assertEquals(1, vetService.getVetsByExperience(5).size());
    }

    @Test
    void filterVetsDelegatesToSpecificationRepository() {
        Vet vet = Vet.builder()
                .id(1L)
                .username("drpaws")
                .name("Dr Paws")
                .specialization("Surgery")
                .clinicAddress("Chennai")
                .availableTime("10 AM - 12 PM")
                .availableDays("Mon")
                .build();

        when(vetRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(List.of(vet));

        assertEquals(1, vetService.filterVets(" Chennai ", 4, " surgery ").size());
    }

    @Test
    void getConsultationFeeReturnsFee() {
        Vet vet = Vet.builder()
                .id(1L)
                .consultationFee(BigDecimal.valueOf(800))
                .build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(vet));

        assertEquals(BigDecimal.valueOf(800), vetService.getConsultationFee(1L).getConsultationFee());
    }

    @Test
    void getConsultationFeeUsesDefaultWhenFeeMissing() {
        when(vetRepository.findById(1L)).thenReturn(Optional.of(Vet.builder().id(1L).build()));

        assertEquals(BigDecimal.valueOf(500), vetService.getConsultationFee(1L).getConsultationFee());
    }

    @Test
    void getAvailableSlotsReturnsHourlySlots() {
        Vet vet = Vet.builder()
                .id(1L)
                .availableTime("10 AM - 1 PM")
                .build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(vet));
        when(vetLeaveRepository.existsByVetIdAndLeaveDate(1L, "2026-05-09")).thenReturn(false);

        assertEquals(List.of("10 AM - 11 AM", "11 AM - 12 PM", "12 PM - 1 PM"),
                vetService.getAvailableSlots(1L, "2026-05-09"));
    }

    @Test
    void getAvailableSlotsReturnsEmptyWhenVetOnLeave() {
        Vet vet = Vet.builder().id(1L).availableTime("10 AM - 1 PM").build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(vet));
        when(vetLeaveRepository.existsByVetIdAndLeaveDate(1L, "2026-05-09")).thenReturn(true);

        assertEquals(List.of(), vetService.getAvailableSlots(1L, "2026-05-09"));
    }

    @Test
    void addLeaveReusesExistingLeave() {
        VetLeave leave = VetLeave.builder().id(3L).vetId(1L).leaveDate("2026-05-09").build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(Vet.builder().id(1L).build()));
        when(vetLeaveRepository.findByVetIdAndLeaveDate(1L, "2026-05-09")).thenReturn(Optional.of(leave));

        assertEquals("2026-05-09", vetService.addLeave(1L, "2026-05-09").getDate());
    }

    @Test
    void getLeavesReturnsSortedLeaves() {
        VetLeave leave = VetLeave.builder().id(3L).vetId(1L).leaveDate("2026-05-09").build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(Vet.builder().id(1L).build()));
        when(vetLeaveRepository.findByVetIdOrderByLeaveDateAsc(1L)).thenReturn(List.of(leave));

        assertEquals(List.of("2026-05-09"), vetService.getLeaveDates(1L));
    }

    @Test
    void addLeaveThrowsWhenVetMissing() {
        when(vetRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(VetNotFoundException.class, () -> vetService.addLeave(1L, "2026-05-09"));
    }
}
