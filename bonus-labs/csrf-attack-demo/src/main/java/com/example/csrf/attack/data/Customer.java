package com.example.csrf.attack.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity
public class Customer extends AbstractPersistable<Long> {

  @NotBlank
  @Size(max = 100)
  private String firstName;

  @NotBlank
  @Size(max = 100)
  private String lastName;

  @NotNull
  private UUID identifier;

  public Customer() {
  }

  public Customer(UUID identifier, String firstName, String lastName) {
    this.identifier = identifier;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
  }

  @JsonIgnore
  @Override
  public Long getId() {
    return super.getId();
  }

  @JsonIgnore
  @Override
  public boolean isNew() {
    return super.isNew();
  }

  @Override
  public String toString() {
    return "Customer{" +
            "identifier='" + identifier + '\'' +
            "firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            '}';
  }
}
