package com.example.csrf.attack.service;

import com.example.csrf.attack.data.Customer;
import com.example.csrf.attack.data.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
@Service
public class CustomerService {

  private final CustomerRepository customerRepository;

  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public List<Customer> findAll() {
    return customerRepository.findAll();
  }

  @Transactional
  public Customer save(Customer entity) {
    return customerRepository.save(entity);
  }

  public Optional<Customer> findByIdentifier(UUID identifier) {
    return customerRepository.findByIdentifier(identifier);
  }

  @Transactional
  public boolean deleteByIdentifier(UUID identifier) {
    return customerRepository.findByIdentifier(identifier)
            .map(customer -> { customerRepository.delete(customer); return true; })
            .orElse(false);
  }
}
