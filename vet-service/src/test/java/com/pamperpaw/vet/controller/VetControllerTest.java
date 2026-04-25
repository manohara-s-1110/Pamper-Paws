package com.pamperpaw.vet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.vet.dto.VetDTO;
import com.pamperpaw.vet.exception.GlobalExceptionHandler;
import com.pamperpaw.vet.exception.VetNotFoundException;
import com.pamperpaw.vet.service.VetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VetControllerTest {

    @Mock
    private VetService vetService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private VetController vetController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        vetController = new VetController(vetService);
        mockMvc = MockMvcBuilders.standaloneSetup(vetController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createVetReturnsBody() throws Exception {
        VetDTO dto = buildVet();
        when(vetService.createVet(dto)).thenReturn(dto);

        mockMvc.perform(post("/vets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr Rex"));
    }

    @Test
    void createVetValidatesRequest() throws Exception {
        VetDTO dto = new VetDTO();
        dto.setExperience(-1);

        mockMvc.perform(post("/vets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getEndpointsReturnData() throws Exception {
        VetDTO dto = buildVet();
        when(vetService.getAllVets()).thenReturn(List.of(dto));
        when(vetService.getVetById(1L)).thenReturn(dto);
        when(vetService.getVetsBySpecialization("Surgery")).thenReturn(List.of(dto));
        when(vetService.getVetsByLocation("Chennai")).thenReturn(List.of(dto));
        when(vetService.getVetsByExperience(5)).thenReturn(List.of(dto));

        mockMvc.perform(get("/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialization").value("Surgery"));

        mockMvc.perform(get("/vets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        mockMvc.perform(get("/vets/specialization").param("specialization", "Surgery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dr Rex"));

        mockMvc.perform(get("/vets/location").param("location", "Chennai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clinicAddress").value("City Clinic"));

        mockMvc.perform(get("/vets/experience").param("experience", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].experience").value(5));
    }

    @Test
    void updateAndDeleteEndpointsWork() throws Exception {
        VetDTO dto = buildVet();
        when(vetService.updateVet(1L, dto)).thenReturn(dto);

        mockMvc.perform(put("/vets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("vet@test.com"));

        mockMvc.perform(delete("/vets/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Vet deleted successfully"));

        verify(vetService).deleteVet(1L);
    }

    @Test
    void asyncEndpointDelegatesToService() {
        CompletableFuture<List<VetDTO>> future = CompletableFuture.completedFuture(List.of(buildVet()));
        when(vetService.getAllVetsAsync()).thenReturn(future);

        assertSame(future, vetController.getAllVetsAsync());
    }

    @Test
    void vetNotFoundIsHandled() throws Exception {
        when(vetService.getVetById(99L)).thenThrow(new VetNotFoundException("Vet not found"));

        mockMvc.perform(get("/vets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vet not found"));
    }

    private VetDTO buildVet() {
        return VetDTO.builder()
                .id(1L)
                .name("Dr Rex")
                .specialization("Surgery")
                .experience(5)
                .phone("9876543210")
                .email("vet@test.com")
                .clinicAddress("City Clinic")
                .availableDays("Mon-Fri")
                .availableTime("10AM-6PM")
                .build();
    }
}
