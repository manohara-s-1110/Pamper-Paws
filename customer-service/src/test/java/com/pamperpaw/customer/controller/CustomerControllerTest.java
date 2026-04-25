package com.pamperpaw.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.customer.dto.CustomerDTO;
import com.pamperpaw.customer.exception.GlobalExceptionHandler;
import com.pamperpaw.customer.exception.ResourceNotFoundException;
import com.pamperpaw.customer.service.CustomerService;
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
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomerController(customerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createCustomerReturnsBody() throws Exception {
        CustomerDTO dto = buildCustomer();
        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Manu"));
    }

    @Test
    void createCustomerValidatesRequest() throws Exception {
        CustomerDTO dto = new CustomerDTO();

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getEndpointsReturnCustomers() throws Exception {
        CustomerDTO dto = buildCustomer();
        when(customerService.getAllCustomers()).thenReturn(List.of(dto));
        when(customerService.getCustomerById(1L)).thenReturn(dto);

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("manu@test.com"));

        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateCustomerReturnsUpdatedCustomer() throws Exception {
        CustomerDTO dto = buildCustomer();
        when(customerService.updateCustomer(org.mockito.ArgumentMatchers.eq(1L), any(CustomerDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Chennai"));
    }

    @Test
    void deleteCustomerReturnsMessage() throws Exception {
        mockMvc.perform(delete("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer deleted successfully"));

        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void resourceNotFoundIsHandled() throws Exception {
        when(customerService.getCustomerById(42L)).thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/customers/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    private CustomerDTO buildCustomer() {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        dto.setName("Manu");
        dto.setEmail("manu@test.com");
        dto.setPhone("9876543210");
        dto.setAddress("Chennai");
        return dto;
    }
}
