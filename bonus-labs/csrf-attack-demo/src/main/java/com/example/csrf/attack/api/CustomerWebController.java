package com.example.csrf.attack.api;

import com.example.csrf.attack.data.Customer;
import com.example.csrf.attack.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
public class CustomerWebController {
  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerWebController.class);

  private final CustomerService customerService;
  private final IdGenerator idGenerator;

  public CustomerWebController(CustomerService customerService, IdGenerator idGenerator) {
    this.customerService = customerService;
    this.idGenerator = idGenerator;
  }

  @GetMapping
  String index() {
    return "index";
  }

  @ModelAttribute("allCustomers")
  public List<Customer> populateCustomers() {
    return this.customerService.findAll();
  }

  @PostMapping(path = "/web/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void create(CustomerRequest customerRequest) {

    LOGGER.info("Creating new customer via POST request '/web': {}", customerRequest);

    Customer customer = new Customer(idGenerator.generateId(), customerRequest.getFirstName(), customerRequest.getLastName());
    customerService.save(customer);
  }

  @GetMapping("/web/form")
  public String customerForm(Model model) {
    model.addAttribute("customer", new CustomerRequest());
    return "customerform";
  }
}
