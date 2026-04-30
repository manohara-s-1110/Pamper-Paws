package com.pamperpaw.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.admin.dto.CustomerDTO;
import com.pamperpaw.admin.dto.PetDTO;
import com.pamperpaw.admin.dto.VetDTO;
import com.pamperpaw.admin.dto.VisitDTO;
import com.pamperpaw.admin.entity.Admin;
import com.pamperpaw.admin.exception.GlobalExceptionHandler;
import com.pamperpaw.admin.exception.ResourceNotFoundException;
import com.pamperpaw.admin.service.AdminService;
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
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(adminService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void addAdminReturnsCreatedAdmin() throws Exception {
        Admin admin = buildAdmin();
        when(adminService.addAdmin(admin)).thenReturn(admin);

        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin User"));
    }

    @Test
    void addAdminReturnsValidationError() throws Exception {
        Admin admin = new Admin();
        admin.setEmail("bad-email");

        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getAllAdminsReturnsList() throws Exception {
        when(adminService.getAllAdmins()).thenReturn(List.of(buildAdmin()));

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@pamperpaw.com"));
    }

    @Test
    void getAdminByIdReturnsAdmin() throws Exception {
        when(adminService.getAdminById(1L)).thenReturn(buildAdmin());

        mockMvc.perform(get("/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void updateAdminReturnsUpdatedAdmin() throws Exception {
        Admin admin = buildAdmin();
        when(adminService.updateAdmin(1L, admin)).thenReturn(admin);

        mockMvc.perform(put("/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin User"));
    }

    @Test
    void deleteAdminReturnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/admin/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin deleted successfully"));

        verify(adminService).deleteAdmin(1L);
    }

    @Test
    void getAllCustomersReturnsList() throws Exception {
        when(adminService.getAllCustomers()).thenReturn(List.of(CustomerDTO.builder().name("Manu").build()));

        mockMvc.perform(get("/admin/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Manu"));
    }

    @Test
    void getPetsByCustomerReturnsList() throws Exception {
        when(adminService.getPetsByCustomer(1L)).thenReturn(List.of(PetDTO.builder().name("Bruno").build()));

        mockMvc.perform(get("/admin/customers/1/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bruno"));
    }

    @Test
    void getAllVetsReturnsList() throws Exception {
        when(adminService.getAllVets()).thenReturn(List.of(VetDTO.builder().name("Dr Rex").build()));

        mockMvc.perform(get("/admin/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dr Rex"));
    }

    @Test
    void deleteUserReturnsMessage() throws Exception {
        when(adminService.deleteUser(2L)).thenReturn("Customer deleted successfully");

        mockMvc.perform(delete("/admin/users/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer deleted successfully"));
    }

    @Test
    void visitEndpointsReturnData() throws Exception {
        VisitDTO visit = VisitDTO.builder().id(10L).reason("Checkup").build();
        when(adminService.getAllVisits()).thenReturn(List.of(visit));
        when(adminService.getVisitById(10L)).thenReturn(visit);

        mockMvc.perform(get("/admin/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Checkup"));

        mockMvc.perform(get("/admin/visits/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void deleteVisitReturnsMessage() throws Exception {
        mockMvc.perform(delete("/admin/visits/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Visit deleted successfully"));

        verify(adminService).deleteVisit(5L);
    }

    @Test
    void resourceNotFoundIsHandled() throws Exception {
        when(adminService.getAdminById(99L)).thenThrow(new ResourceNotFoundException("Admin not found"));

        mockMvc.perform(get("/admin/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Admin not found"));
    }

    private Admin buildAdmin() {
        return Admin.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@pamperpaw.com")
                .password("secret")
                .role("ADMIN")
                .build();
    }
}
