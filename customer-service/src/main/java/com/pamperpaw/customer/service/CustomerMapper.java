package com.pamperpaw.customer.service;

import com.pamperpaw.customer.dto.CustomerDTO;
import com.pamperpaw.customer.entity.Customer;

public class CustomerMapper {

    public static Customer toEntity(CustomerDTO dto) {
        Customer customer = new Customer();

        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setUsername(dto.getUsername());
        return customer;
    }

    public static CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();

        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setUsername(customer.getUsername());

        return dto;
    }
}