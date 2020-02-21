package com.example.csrf.attack.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

  Optional<Customer> findByIdentifier(UUID identifier);
}
