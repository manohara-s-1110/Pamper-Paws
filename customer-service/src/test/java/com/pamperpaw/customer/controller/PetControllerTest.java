package com.pamperpaw.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.customer.dto.PetDTO;
import com.pamperpaw.customer.exception.GlobalExceptionHandler;
import com.pamperpaw.customer.service.PetService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    @Mock
    private PetService petService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new PetController(petService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void addPetReturnsPet() throws Exception {
        PetDTO dto = buildPet();
        when(petService.addPet(org.mockito.ArgumentMatchers.eq(1L), any(PetDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/pets/customer/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bruno"));
    }

    @Test
    void addPetValidatesRequest() throws Exception {
        PetDTO dto = new PetDTO();
        dto.setAge(-1);

        mockMvc.perform(post("/pets/customer/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getPetsReturnsList() throws Exception {
        when(petService.getPetsByCustomer(1L)).thenReturn(List.of(buildPet()));

        mockMvc.perform(get("/pets/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("Dog"));
    }

    private PetDTO buildPet() {
        PetDTO dto = new PetDTO();
        dto.setId(1L);
        dto.setName("Bruno");
        dto.setType("Dog");
        dto.setAge(3);
        return dto;
    }
}
