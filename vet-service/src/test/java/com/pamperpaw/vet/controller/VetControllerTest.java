package com.pamperpaw.vet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.vet.dto.VetDTO;
import com.pamperpaw.vet.dto.VetFeeResponse;
import com.pamperpaw.vet.dto.VetLeaveRequest;
import com.pamperpaw.vet.dto.VetLeaveResponse;
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
import java.math.BigDecimal;

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
        when(vetService.getVetByUsername("drrex")).thenReturn(dto);
        when(vetService.getVetsBySpecialization("Surgery")).thenReturn(List.of(dto));
        when(vetService.getVetsByLocation("Chennai")).thenReturn(List.of(dto));
        when(vetService.getVetsByExperience(5)).thenReturn(List.of(dto));
        when(vetService.filterVets("Chennai", 5, "Surgery")).thenReturn(List.of(dto));
        when(vetService.getAvailableSlots(1L, "2026-05-09")).thenReturn(List.of("10 AM - 11 AM"));
        when(vetService.getConsultationFee(1L)).thenReturn(new VetFeeResponse(1L, BigDecimal.valueOf(500)));

        mockMvc.perform(get("/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialization").value("Surgery"));

        mockMvc.perform(get("/vets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        mockMvc.perform(get("/vets/username/drrex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("drrex"));

        mockMvc.perform(get("/vets/specialization").param("specialization", "Surgery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dr Rex"));

        mockMvc.perform(get("/vets/location").param("location", "Chennai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clinicAddress").value("City Clinic"));

        mockMvc.perform(get("/vets/experience").param("experience", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].experience").value(5));

        mockMvc.perform(get("/vets/filter")
                        .param("location", "Chennai")
                        .param("experience", "5")
                        .param("specialization", "Surgery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dr Rex"));

        mockMvc.perform(get("/vets/1/slots").param("date", "2026-05-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("10 AM - 11 AM"));

        mockMvc.perform(get("/vets/1/fee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultationFee").value(500));
    }

    @Test
    void leaveEndpointsDelegateToService() throws Exception {
        VetLeaveRequest request = new VetLeaveRequest();
        request.setDate("2026-05-09");
        VetLeaveResponse response = VetLeaveResponse.builder()
                .id(1L)
                .vetId(1L)
                .date("2026-05-09")
                .build();

        when(vetService.addLeave(1L, "2026-05-09")).thenReturn(response);
        when(vetService.getLeaves(1L)).thenReturn(List.of(response));
        when(vetService.getLeaveDates(1L)).thenReturn(List.of("2026-05-09"));

        mockMvc.perform(post("/vets/1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-05-09"));

        mockMvc.perform(get("/vets/1/leave-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-05-09"));

        mockMvc.perform(get("/vets/1/leaves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("2026-05-09"));
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
                .username("drrex")
                .name("Dr Rex")
                .specialization("Surgery")
                .experience(5)
                .phone("9876543210")
                .email("vet@test.com")
                .clinicAddress("City Clinic")
                .availableDays("Mon-Fri")
                .availableTime("10AM-6PM")
                .consultationFee(BigDecimal.valueOf(500))
                .build();
    }
}
