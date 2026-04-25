package com.pamperpaw.customer.service;

import java.util.List;

import com.pamperpaw.customer.dto.CustomerDTO;

public interface CustomerService {

	CustomerDTO createCustomer(CustomerDTO dto);
	List<CustomerDTO> getAllCustomers();
	CustomerDTO getCustomerById(Long id);
	CustomerDTO updateCustomer(Long id, CustomerDTO dto);
	void deleteCustomer(Long id);
	CustomerDTO getCustomerByUsername(String username);
}