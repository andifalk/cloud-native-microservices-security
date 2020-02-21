package com.example.csrf.attack.api;

import com.example.csrf.attack.data.Customer;
import com.example.csrf.attack.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@Controller
@RequestMapping("/web")
public class ApiController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

  private final CustomerService customerService;

  public ApiController(CustomerService customerService) {
    this.customerService = customerService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void create(Customer customer) {

    LOGGER.info("Creating new customer via POST request '/web': {}", customer);

    customer.setIdentifier(UUID.randomUUID());
    customerService.save(customer);
  }

  @GetMapping("/form")
  public String customerForm(Model model) {
    model.addAttribute("customer", new Customer());
    return "customerform";
  }
}
