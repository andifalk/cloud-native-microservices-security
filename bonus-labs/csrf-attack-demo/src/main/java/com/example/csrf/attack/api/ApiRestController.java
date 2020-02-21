package com.example.csrf.attack.api;

import com.example.csrf.attack.data.Customer;
import com.example.csrf.attack.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Validated
public class ApiRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiRestController.class);

  private final CustomerService customerService;

  public ApiRestController(CustomerService customerService) {
    this.customerService = customerService;
  }

  @GetMapping
  public List<Customer> findAll() {
    return customerService.findAll();
  }

  @PostMapping
  public Customer create(@Valid @RequestBody Customer entity) {
    return customerService.save(entity);
  }

  // Do NOT map modifying requests to GET requests !!!!!!!!!!!
  @GetMapping("/create")
  public Customer create(@Size(min = 1, max = 100) @RequestParam("firstname") String firstName,
                         @Size(min = 1, max = 100) @RequestParam("lastname") String lastName) {

    Customer customer = new Customer(UUID.randomUUID(), firstName, lastName);
    LOGGER.info("Creating new customer via GET request '/api/create': {}", customer);

    return customerService.save(customer);
  }

  @GetMapping("/{customerIdentifier}")
  public Optional<Customer> findByIdentifier(@PathVariable("customerIdentifier") UUID identifier) {
    return customerService.findByIdentifier(identifier);
  }

  @DeleteMapping("/{customerIdentifier}")
  public boolean deleteByIdentifier(UUID identifier) {
    return customerService.deleteByIdentifier(identifier);
  }
}
