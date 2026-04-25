package com.pamperpaw.admin.service.impl;

import com.pamperpaw.admin.client.CustomerClient;
import com.pamperpaw.admin.client.VetClient;
import com.pamperpaw.admin.client.VisitClient;
import com.pamperpaw.admin.dto.CustomerDTO;
import com.pamperpaw.admin.dto.PetDTO;
import com.pamperpaw.admin.dto.VetDTO;
import com.pamperpaw.admin.dto.VisitDTO;
import com.pamperpaw.admin.entity.Admin;
import com.pamperpaw.admin.exception.DuplicateResourceException;
import com.pamperpaw.admin.exception.ResourceNotFoundException;
import com.pamperpaw.admin.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private VetClient vetClient;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private VisitClient visitClient;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void addAdminRejectsDuplicateEmail() {
        Admin admin = Admin.builder().email("admin@test.com").build();
        when(adminRepository.existsByEmail("admin@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> adminService.addAdmin(admin));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void getAdminByIdThrowsWhenMissing() {
        when(adminRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getAdminById(10L));
    }

    @Test
    void getAllCustomersDelegatesToFeignClient() {
        when(customerClient.getAllUsers()).thenReturn(List.of(new CustomerDTO()));

        assertEquals(1, adminService.getAllCustomers().size());
    }

    @Test
    void getPetsByCustomerDelegatesToFeignClient() {
        when(customerClient.getPetsByCustomer(1L)).thenReturn(List.of(new PetDTO()));

        assertEquals(1, adminService.getPetsByCustomer(1L).size());
    }

    @Test
    void getAllVetsAndVisitsDelegateToFeignClients() {
        when(vetClient.getAllVets()).thenReturn(List.of(new VetDTO()));
        when(visitClient.getAllVisits()).thenReturn(List.of(new VisitDTO()));

        assertEquals(1, adminService.getAllVets().size());
        assertEquals(1, adminService.getAllVisits().size());
    }

    @Test
    void addAdminSavesNewAdmin() {
        Admin admin = Admin.builder().id(1L).email("admin@test.com").name("Admin").password("secret").role("ADMIN").build();
        when(adminRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(adminRepository.save(admin)).thenReturn(admin);

        Admin saved = adminService.addAdmin(admin);

        assertEquals(1L, saved.getId());
        assertEquals("Admin", saved.getName());
    }

    @Test
    void getAllAdminsReadsRepository() {
        when(adminRepository.findAll()).thenReturn(List.of(Admin.builder().id(1L).build()));

        assertEquals(1, adminService.getAllAdmins().size());
    }

    @Test
    void getAdminByIdReturnsAdminWhenFound() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(Admin.builder().id(1L).name("Admin").build()));

        assertEquals("Admin", adminService.getAdminById(1L).getName());
    }

    @Test
    void updateAdminPersistsUpdatedFields() {
        Admin existing = Admin.builder().id(1L).name("Old").email("old@test.com").password("old").role("ADMIN").build();
        Admin update = Admin.builder().name("New").email("new@test.com").password("new").role("ADMIN").build();
        Admin saved = Admin.builder().id(1L).name("New").email("new@test.com").password("new").role("ADMIN").build();

        when(adminRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(adminRepository.save(existing)).thenReturn(saved);

        Admin response = adminService.updateAdmin(1L, update);

        assertEquals("New", response.getName());
        assertEquals("new@test.com", response.getEmail());
    }

    @Test
    void deleteAdminDeletesExistingAdmin() {
        Admin admin = Admin.builder().id(1L).build();
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        adminService.deleteAdmin(1L);

        verify(adminRepository).delete(admin);
    }

    @Test
    void deleteUserAndVisitsDelegateToFeignClients() {
        VisitDTO visit = new VisitDTO();
        when(customerClient.deleteUser(3L)).thenReturn("deleted");
        when(visitClient.getVisitById(5L)).thenReturn(visit);

        assertEquals("deleted", adminService.deleteUser(3L));
        assertEquals(visit, adminService.getVisitById(5L));

        adminService.deleteVisit(5L);

        verify(visitClient).deleteVisit(5L);
    }
}
