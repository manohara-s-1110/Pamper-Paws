package com.pamperpaw.customer.service.impl;

import com.pamperpaw.customer.dto.CustomerDTO;
import com.pamperpaw.customer.entity.Customer;
import com.pamperpaw.customer.exception.DuplicateResourceException;
import com.pamperpaw.customer.exception.ResourceNotFoundException;
import com.pamperpaw.customer.repository.CustomerRepository;
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
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createCustomerSavesWhenEmailIsUnique() {
        CustomerDTO dto = new CustomerDTO();
        dto.setName("Manu");
        dto.setEmail("manu@test.com");
        dto.setPhone("9876543210");
        dto.setAddress("Chennai");

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName(dto.getName());
        savedCustomer.setEmail(dto.getEmail());
        savedCustomer.setPhone(dto.getPhone());
        savedCustomer.setAddress(dto.getAddress());

        when(customerRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerDTO response = customerService.createCustomer(dto);

        assertEquals(1L, response.getId());
        assertEquals(dto.getEmail(), response.getEmail());
    }

    @Test
    void createCustomerRejectsDuplicateEmail() {
        CustomerDTO dto = new CustomerDTO();
        dto.setEmail("manu@test.com");

        when(customerRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new Customer()));

        assertThrows(DuplicateResourceException.class, () -> customerService.createCustomer(dto));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerByIdThrowsWhenMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(99L));
    }

    @Test
    void getAllCustomersMapsEntities() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Manu");
        customer.setEmail("manu@test.com");
        customer.setPhone("9876543210");
        customer.setAddress("Chennai");

        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<CustomerDTO> response = customerService.getAllCustomers();

        assertEquals(1, response.size());
        assertEquals("Manu", response.get(0).getName());
    }

    @Test
    void deleteCustomerThrowsWhenMissing() {
        when(customerRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(7L));
    }

    @Test
    void getCustomerByIdReturnsMappedCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Manu");
        customer.setEmail("manu@test.com");
        customer.setPhone("9876543210");
        customer.setAddress("Chennai");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerDTO response = customerService.getCustomerById(1L);

        assertEquals("Manu", response.getName());
        assertEquals("Chennai", response.getAddress());
    }

    @Test
    void updateCustomerUpdatesExistingRecord() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setName("Old");
        existing.setEmail("old@test.com");

        Customer updated = new Customer();
        updated.setId(1L);
        updated.setName("New");
        updated.setEmail("new@test.com");
        updated.setPhone("9999999999");
        updated.setAddress("Madurai");

        CustomerDTO dto = new CustomerDTO();
        dto.setName("New");
        dto.setEmail("new@test.com");
        dto.setPhone("9999999999");
        dto.setAddress("Madurai");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(updated);

        CustomerDTO response = customerService.updateCustomer(1L, dto);

        assertEquals("New", response.getName());
        assertEquals("Madurai", response.getAddress());
    }

    @Test
    void updateCustomerThrowsWhenMissing() {
        when(customerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.updateCustomer(5L, new CustomerDTO()));
    }

    @Test
    void deleteCustomerDeletesExistingRecord() {
        Customer customer = new Customer();
        customer.setId(3L);

        when(customerRepository.findById(3L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(3L);

        verify(customerRepository).delete(customer);
    }
}
