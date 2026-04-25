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
import com.pamperpaw.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final VetClient vetClient;
    private final CustomerClient userClient;
    private final VisitClient visitClient;

    @Override
    public Admin addAdmin(Admin admin) {
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new DuplicateResourceException("Admin already exists with email: " + admin.getEmail());
        }
        Admin savedAdmin = adminRepository.save(admin);
        log.info("Created admin with id={}", savedAdmin.getId());
        return savedAdmin;
    }

    @Override
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }

    @Override
    public Admin updateAdmin(Long id, Admin admin) {
        Admin existing = getAdminById(id);
        existing.setName(admin.getName());
        existing.setEmail(admin.getEmail());
        existing.setPassword(admin.getPassword());
        existing.setRole(admin.getRole());
        Admin updatedAdmin = adminRepository.save(existing);
        log.info("Updated admin with id={}", updatedAdmin.getId());
        return updatedAdmin;
    }

    @Override
    public void deleteAdmin(Long id) {
        Admin admin = getAdminById(id);
        adminRepository.delete(admin);
        log.info("Deleted admin with id={}", id);
    }

    @Override
    public List<VetDTO> getAllVets() {
        return vetClient.getAllVets();
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return userClient.getAllUsers();
    }

    @Override
    public List<PetDTO> getPetsByCustomer(Long customerId) {
        return userClient.getPetsByCustomer(customerId);
    }

    @Override
    public String deleteUser(Long userId) {
        return userClient.deleteUser(userId);
    }

    @Override
    public List<VisitDTO> getAllVisits() {
        return visitClient.getAllVisits();
    }

    @Override
    public VisitDTO getVisitById(Long id) {
        return visitClient.getVisitById(id);
    }

    @Override
    public void deleteVisit(Long id) {
        visitClient.deleteVisit(id);
    }
}
