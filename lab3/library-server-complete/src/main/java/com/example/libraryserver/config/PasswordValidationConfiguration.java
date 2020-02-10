package com.example.libraryserver.config;

import com.example.libraryserver.user.service.PasswordValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordValidationConfiguration {

  @Bean
  public PasswordValidationService passwordValidationService() {
    return new PasswordValidationService();
  }
}
