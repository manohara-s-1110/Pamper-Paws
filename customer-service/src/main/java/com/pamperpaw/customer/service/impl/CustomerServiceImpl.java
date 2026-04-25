package com.pamperpaw.customer.service.impl;

import com.pamperpaw.customer.dto.CustomerDTO;
import com.pamperpaw.customer.entity.Customer;
import com.pamperpaw.customer.exception.DuplicateResourceException;
import com.pamperpaw.customer.exception.ResourceNotFoundException;
import com.pamperpaw.customer.repository.CustomerRepository;
import com.pamperpaw.customer.service.CustomerMapper;
import com.pamperpaw.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;

    @Override
    public CustomerDTO createCustomer(CustomerDTO dto) {
    	
    	log.info("Incoming DTO: {}", dto);
    	
    	// ✅ Check duplicate email
        repo.findByEmail(dto.getEmail()).ifPresent(customer -> {
            throw new DuplicateResourceException("Customer already exists with email: " + dto.getEmail());
        });

        // ✅ ADD THIS BLOCK (username validation)
        if (repo.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }
        
        Customer customer = CustomerMapper.toEntity(dto);
        Customer saved = repo.save(customer);
        log.info("Creating customer with username={}", dto.getUsername());
        log.info("Created customer with id={}", saved.getId());
        
        return CustomerMapper.toDTO(saved);
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        log.info("Fetching all customers");
        return repo.findAll()
                .stream()
                .map(CustomerMapper::toDTO)
                .toList();
    }

    @Override
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return CustomerMapper.toDTO(customer);
    }

    @Override
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        Customer existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setAddress(dto.getAddress());

        Customer updated = repo.save(existing);
        log.info("Updated customer with id={}", updated.getId());
        return CustomerMapper.toDTO(updated);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        repo.delete(customer);
        log.info("Deleted customer with id={}", id);
    }
    
    @Override
    public CustomerDTO getCustomerByUsername(String username) {
        Customer customer = repo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return CustomerMapper.toDTO(customer);
    }
}
