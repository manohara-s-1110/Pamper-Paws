package com.pamperpaw.visit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.visit.dto.VisitRequestDTO;
import com.pamperpaw.visit.dto.VisitResponseDTO;
import com.pamperpaw.visit.exception.GlobalExceptionHandler;
import com.pamperpaw.visit.exception.ResourceNotFoundException;
import com.pamperpaw.visit.service.VisitService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VisitControllerTest {

    @Mock
    private VisitService visitService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private VisitController visitController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        visitController = new VisitController(visitService);
        mockMvc = MockMvcBuilders.standaloneSetup(visitController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createVisitReturnsBody() throws Exception {
        VisitRequestDTO request = buildVisitRequest();
        VisitResponseDTO response = buildVisitResponse();
        when(visitService.createVisit(request)).thenReturn(response);

        mockMvc.perform(post("/visit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Checkup"));
    }

    @Test
    void createVisitValidatesRequest() throws Exception {
        VisitRequestDTO request = new VisitRequestDTO();

        mockMvc.perform(post("/visit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getEndpointsReturnVisits() throws Exception {
        VisitResponseDTO response = buildVisitResponse();
        when(visitService.getAllVisits()).thenReturn(List.of(response));
        when(visitService.getVisitById(1L)).thenReturn(response);

        mockMvc.perform(get("/visit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1L));

        mockMvc.perform(get("/visit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vetId").value(2L));
    }

    @Test
    void asyncEndpointDelegatesToService() {
        CompletableFuture<List<VisitResponseDTO>> future = CompletableFuture.completedFuture(List.of(buildVisitResponse()));
        when(visitService.getAllVisitsAsync()).thenReturn(future);

        assertSame(future, visitController.getAllVisitsAsync());
    }

    @Test
    void deleteVisitDelegatesToService() throws Exception {
        mockMvc.perform(delete("/visit/1"))
                .andExpect(status().isOk());

        verify(visitService).deleteVisit(1L);
    }

    @Test
    void resourceNotFoundIsHandled() throws Exception {
        when(visitService.getVisitById(99L)).thenThrow(new ResourceNotFoundException("Visit not found"));

        mockMvc.perform(get("/visit/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Visit not found"));
    }

    private VisitRequestDTO buildVisitRequest() {
        VisitRequestDTO dto = new VisitRequestDTO();
        dto.setCustomerId(1L);
        dto.setVetId(2L);
        dto.setVisitDate("2026-04-13");
        dto.setReason("Checkup");
        return dto;
    }

    private VisitResponseDTO buildVisitResponse() {
        VisitResponseDTO dto = new VisitResponseDTO();
        dto.setId(1L);
        dto.setCustomerId(1L);
        dto.setVetId(2L);
        dto.setVisitDate("2026-04-13");
        dto.setReason("Checkup");
        return dto;
    }
}
