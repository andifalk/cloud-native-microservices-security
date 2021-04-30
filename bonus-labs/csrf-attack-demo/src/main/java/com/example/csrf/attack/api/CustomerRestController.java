package com.example.csrf.attack.api;

import com.example.csrf.attack.data.Customer;
import com.example.csrf.attack.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.IdGenerator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api")
@Validated
public class CustomerRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerRestController.class);

  private final CustomerService customerService;
  private final IdGenerator idGenerator;

  public CustomerRestController(CustomerService customerService, IdGenerator idGenerator) {
    this.customerService = customerService;
    this.idGenerator = idGenerator;
  }

  @GetMapping
  public List<Customer> findAll() {
    return customerService.findAll();
  }

  @ResponseStatus(NO_CONTENT)
  @PostMapping(path = "/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.ALL_VALUE)
  public String create(CustomerRequest customerRequest) {

    customerService.save(new Customer(idGenerator.generateId(), customerRequest.getFirstName(), customerRequest.getLastName()));
    LOGGER.info("Creating new customer via POST request '/api/create': {}", customerRequest);
    return "Created";
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
