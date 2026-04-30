package com.pamperpaw.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.pamperpaw.admin.dto.CustomerDTO;
import com.pamperpaw.admin.dto.PetDTO;

import java.util.List;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    @GetMapping("/customers")
    List<CustomerDTO> getAllUsers();

    @GetMapping("/customers/{id}")
    CustomerDTO getCustomerById(@PathVariable("id") Long id);

    @DeleteMapping("/customers/{id}")
    String deleteUser(@PathVariable("id") Long id);

    @GetMapping("/pets/customer/{customerId}")
    List<PetDTO> getPetsByCustomer(@PathVariable("customerId") Long customerId);
}
