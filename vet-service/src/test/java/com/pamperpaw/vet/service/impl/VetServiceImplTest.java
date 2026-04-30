package com.pamperpaw.vet.service.impl;

import com.pamperpaw.vet.dto.VetDTO;
import com.pamperpaw.vet.entity.Vet;
import com.pamperpaw.vet.exception.VetNotFoundException;
import com.pamperpaw.vet.repository.VetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VetServiceImplTest {

    @Mock
    private VetRepository vetRepository;

    @InjectMocks
    private VetServiceImpl vetService;

    @Test
    void createVetPersistsAndMapsResponse() {
        VetDTO dto = VetDTO.builder()
                .name("Dr Paws")
                .specialization("Surgery")
                .experience(5)
                .phone("9876543210")
                .email("paws@test.com")
                .clinicAddress("Chennai")
                .availableDays("Mon")
                .availableTime("10AM")
                .build();

        Vet savedVet = Vet.builder()
                .id(1L)
                .name(dto.getName())
                .specialization(dto.getSpecialization())
                .experience(dto.getExperience())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .clinicAddress(dto.getClinicAddress())
                .availableDays(dto.getAvailableDays())
                .availableTime(dto.getAvailableTime())
                .build();

        when(vetRepository.save(any(Vet.class))).thenReturn(savedVet);

        VetDTO response = vetService.createVet(dto);

        assertEquals(1L, response.getId());
        assertEquals("Dr Paws", response.getName());
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
                .specialization("Surgery")
                .experience(5)
                .phone("9876543210")
                .email("paws@test.com")
                .clinicAddress("Chennai")
                .availableDays("Mon")
                .availableTime("10AM")
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
                .specialization("Surgery")
                .experience(5)
                .phone("9876543210")
                .email("paws@test.com")
                .clinicAddress("Chennai")
                .availableDays("Mon")
                .availableTime("10AM")
                .build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(vet));

        VetDTO response = vetService.getVetById(1L);

        assertEquals("Dr Paws", response.getName());
        assertEquals("Chennai", response.getClinicAddress());
    }

    @Test
    void updateVetUpdatesExistingVet() {
        Vet existing = Vet.builder().id(1L).build();
        Vet saved = Vet.builder()
                .id(1L)
                .name("Updated")
                .specialization("Dermatology")
                .experience(8)
                .phone("9999999999")
                .email("updated@test.com")
                .clinicAddress("Madurai")
                .availableDays("Tue")
                .availableTime("11AM")
                .build();

        VetDTO dto = VetDTO.builder()
                .name("Updated")
                .specialization("Dermatology")
                .experience(8)
                .phone("9999999999")
                .email("updated@test.com")
                .clinicAddress("Madurai")
                .availableDays("Tue")
                .availableTime("11AM")
                .build();

        when(vetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vetRepository.save(existing)).thenReturn(saved);

        VetDTO response = vetService.updateVet(1L, dto);

        assertEquals("Updated", response.getName());
        assertEquals("Dermatology", response.getSpecialization());
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
                .specialization("Surgery")
                .experience(5)
                .clinicAddress("Chennai")
                .build();

        when(vetRepository.findBySpecialization("Surgery")).thenReturn(List.of(vet));
        when(vetRepository.findByClinicAddress("Chennai")).thenReturn(List.of(vet));
        when(vetRepository.findByExperienceGreaterThanEqual(5)).thenReturn(List.of(vet));

        assertEquals(1, vetService.getVetsBySpecialization("Surgery").size());
        assertEquals(1, vetService.getVetsByLocation("Chennai").size());
        assertEquals(1, vetService.getVetsByExperience(5).size());
    }
}
