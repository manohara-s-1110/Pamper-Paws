package com.pamperpaw.admin.service;

import java.util.List;

import com.pamperpaw.admin.dto.*;
import com.pamperpaw.admin.entity.Admin;

public interface AdminService {

    Admin addAdmin(Admin admin);
    List<Admin> getAllAdmins();
    Admin getAdminById(Long id);
    Admin updateAdmin(Long id, Admin admin);
    void deleteAdmin(Long id);

    List<VetDTO> getAllVets();

    List<CustomerDTO> getAllCustomers();
    List<PetDTO> getPetsByCustomer(Long customerId);
    String deleteUser(Long userId);
    
    List<VisitDTO> getAllVisits();

    VisitDTO getVisitById(Long id);

    void deleteVisit(Long id);
}