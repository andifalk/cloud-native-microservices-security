package com.example.libraryserver.user.service;

import java.util.List;

public class InvalidPasswordError extends RuntimeException {

  private List<String> validationErrors;

  public InvalidPasswordError(List<String> validationErrors) {
    super("Validation failed: " + String.join(",", validationErrors));
    this.validationErrors = validationErrors;
  }

  public List<String> getValidationErrors() {
    return validationErrors;
  }
}
