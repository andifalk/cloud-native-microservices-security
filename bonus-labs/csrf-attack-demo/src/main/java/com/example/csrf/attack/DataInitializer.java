package com.example.csrf.attack;

import com.example.csrf.attack.data.Customer;
import com.example.csrf.attack.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DataInitializer implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

  private static final UUID CUSTOMER_1_ID = UUID.fromString("70e8efbf-06ca-4520-9fbd-5762daa01348");
  private static final UUID CUSTOMER_2_ID = UUID.fromString("bf93921b-d2de-432c-87eb-0c017349a427");
  private static final UUID CUSTOMER_3_ID = UUID.fromString("06af7967-310b-4ebb-9ac0-cd2a790dd0b4");
  private static final UUID CUSTOMER_4_ID = UUID.fromString("62bcbf30-3240-4886-9330-5af727b407ce");

  private final CustomerService customerService;

  public DataInitializer(CustomerService customerService) {
    this.customerService = customerService;
  }

  @Override
  public void run(String... args) {
    List<Customer> customers = Stream.of(
            new Customer(CUSTOMER_1_ID, "Hans", "Test1"),
            new Customer(CUSTOMER_2_ID, "Hans", "Test2"),
            new Customer(CUSTOMER_3_ID, "Hans", "Test3"),
            new Customer(CUSTOMER_4_ID, "Hans", "Test4")
    ).map(customerService::save).collect(Collectors.toList());
    LOGGER.info("Created {} customers", customers.size());
  }
}
