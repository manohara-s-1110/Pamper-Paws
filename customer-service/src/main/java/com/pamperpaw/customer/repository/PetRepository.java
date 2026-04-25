package com.pamperpaw.customer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pamperpaw.customer.entity.Customer;
import com.pamperpaw.customer.entity.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {

	List<Pet> findByCustomerId(Long customerId);
}