package com.pamperpaw.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pamperpaw.customer.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUsername(String username);
    boolean existsByUsername(String username);
}