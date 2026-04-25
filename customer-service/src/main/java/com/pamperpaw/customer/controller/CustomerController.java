package com.pamperpaw.customer.controller;

import com.pamperpaw.customer.dto.CustomerDTO;
import com.pamperpaw.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    public CustomerDTO createCustomer(@Valid @RequestBody CustomerDTO dto) {
        return service.createCustomer(dto);
    }

    @GetMapping
    public List<CustomerDTO> getAllCustomers() {
        return service.getAllCustomers();
    }

    @GetMapping("/{id}")
    public CustomerDTO getCustomer(@PathVariable Long id) {
        return service.getCustomerById(id);
    }

    @GetMapping("/username/{username}")
    public CustomerDTO getByUsername(@PathVariable String username) {
        return service.getCustomerByUsername(username);
    }
    
    @PutMapping("/{id}")
    public CustomerDTO updateCustomer(@PathVariable Long id,
                                      @Valid @RequestBody CustomerDTO dto) {
        return service.updateCustomer(id, dto);
    }

    @DeleteMapping("/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        service.deleteCustomer(id);
        return "Customer deleted successfully";
    }
}
