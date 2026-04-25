package com.pamperpaw.admin.controller;

import com.pamperpaw.admin.dto.CustomerDTO;
import com.pamperpaw.admin.dto.PetDTO;
import com.pamperpaw.admin.dto.VetDTO;
import com.pamperpaw.admin.dto.VisitDTO;
import com.pamperpaw.admin.entity.Admin;
import com.pamperpaw.admin.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<Admin> addAdmin(@Valid @RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.addAdmin(admin));
    }

    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable Long id, @Valid @RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.updateAdmin(id, admin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok("Admin deleted successfully");
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        return ResponseEntity.ok(adminService.getAllCustomers());
    }

    @GetMapping("/customers/{customerId}/pets")
    public ResponseEntity<List<PetDTO>> getPets(@PathVariable Long customerId) {
        return ResponseEntity.ok(adminService.getPetsByCustomer(customerId));
    }

    @GetMapping("/vets")
    public ResponseEntity<List<VetDTO>> getAllVets() {
        return ResponseEntity.ok(adminService.getAllVets());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }
    
    @GetMapping("/visits")
    public ResponseEntity<List<VisitDTO>> getAllVisits() {
        return ResponseEntity.ok(adminService.getAllVisits());
    }

    @GetMapping("/visits/{id}")
    public ResponseEntity<VisitDTO> getVisitById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getVisitById(id));
    }

    @DeleteMapping("/visits/{id}")
    public ResponseEntity<String> deleteVisit(@PathVariable Long id) {
        adminService.deleteVisit(id);
        return ResponseEntity.ok("Visit deleted successfully");
    }
}
