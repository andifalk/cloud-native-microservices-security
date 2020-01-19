package com.example.libraryserver.user.web;

import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class UserModel extends RepresentationModel<UserModel> {

  private UUID identifier;

  @NotNull
  @Size(min = 1, max = 50)
  private String firstName;

  @NotNull
  @Size(min = 1, max = 50)
  private String lastName;

  @Email private String email;

  @NotNull
  @Size(min = 1, max = 200)
  private String password;

  private Set<String> roles = new HashSet<>();

  public UserModel() {}

  public UserModel(
          String firstName, String lastName, String email, String password, Set<String> roles) {
    this(null, firstName, lastName, email, password, roles);
  }

  public UserModel(
      UUID identifier, String firstName, String lastName, String email, String password, Set<String> roles) {
    this.identifier = identifier;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.roles = roles;
  }

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }
}
