package com.example.libraryserver.user.web;

import com.example.libraryserver.DataInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest(webEnvironment = MOCK)
@DirtiesContext
@ActiveProfiles("test")
@DisplayName("Calling user rest api")
class UserRestControllerIntegrationTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired private WebApplicationContext context;
  private MockMvc mvc;

  @BeforeEach
  void setup(RestDocumentationContextProvider restDocumentationContextProvider) {
    mvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .apply(
                documentationConfiguration(restDocumentationContextProvider)
                    .operationPreprocessors()
                    .withRequestDefaults(prettyPrint(), modifyUris().port(9090))
                    .withResponseDefaults(prettyPrint(), modifyUris().port(9090)))
            .build();
  }

  @Nested
  @DisplayName("succeeds")
  class PositiveTests {

    @Test
    @DisplayName("in registering a new user")
    void registerUser() throws Exception {
      CreateUserModel model =
          new CreateUserModel(
              "Hans",
              "Mustermann",
              "test@example.com",
              "MySecret4Test",
              Collections.singleton("USER"));
      mvc.perform(
              post("/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model))
                  .with(csrf())
                  .with(user("user")))
          .andExpect(status().isCreated())
          .andExpect(header().exists("location"))
          .andExpect(jsonPath("$.identifier").exists())
          .andDo(document("create-user"));
    }

    @Test
    @DisplayName("in updating an existing user")
    void updateUser() throws Exception {
      CreateUserModel model =
          new CreateUserModel(
              "Hans",
              "Mustermann",
              "test@example.com",
              "MySecret4Test",
              Collections.singleton("LIBRARY_ADMIN"));
      mvc.perform(
              put("/users/{userIdentifier}", DataInitializer.ADMIN_IDENTIFIER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model))
                  .with(csrf())
                  .with(user("user")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.lastName").value("Mustermann"))
          .andDo(document("update-user"));
    }

    @Test
    @DisplayName("in getting a list of all users")
    void listAllUsers() throws Exception {
      mvc.perform(get("/users").with(user("user")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.users.length()").value(greaterThan(0)))
          .andDo(document("get-users"));
    }

    @Test
    @DisplayName("in getting a single user")
    void getSingleUser() throws Exception {
      mvc.perform(
              get("/users/{userIdentifier}", DataInitializer.WAYNE_USER_IDENTIFIER)
                  .with(user("user")))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.identifier").value(DataInitializer.WAYNE_USER_IDENTIFIER.toString()))
          .andExpect(jsonPath("$.lastName").value("Wayne"))
          .andDo(document("get-user"));
    }

    @Test
    @DisplayName("in deleting an user")
    void deleteUser() throws Exception {
      mvc.perform(
              delete("/users/{userIdentifier}", DataInitializer.CURATOR_IDENTIFIER)
                  .with(csrf())
                  .with(user("user")))
          .andExpect(status().isNoContent())
          .andDo(document("delete-user"));
    }
  }

  @Nested
  @DisplayName("fails")
  class NegativeTests {

    @Test
    @DisplayName("in registering a new user with invalid email")
    void registerUser() throws Exception {
      CreateUserModel model =
          new CreateUserModel(
              "Hans", "Mustermann", "example.com", "password", Collections.singleton("USER"));
      mvc.perform(
              post("/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model))
                  .with(csrf())
                  .with(user("user")))
          .andExpect(status().isBadRequest())
          .andExpect(
              content()
                  .string(
                      startsWith(
                          "Field error in object \\'createUserModel\\' on field \\'email\\'")));
    }

    @Test
    @DisplayName("in updating an existing user with invalid email")
    void updateUser() throws Exception {
      CreateUserModel model =
          new CreateUserModel(
              "Hans",
              "Mustermann",
              "example.com",
              "password",
              Collections.singleton("LIBRARY_ADMIN"));
      mvc.perform(
              put("/users/{userIdentifier}", DataInitializer.ADMIN_IDENTIFIER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model))
                  .with(csrf())
                  .with(user("user")))
          .andExpect(status().isBadRequest())
          .andExpect(
              content()
                  .string(
                      startsWith(
                          "Field error in object \\'createUserModel\\' on field \\'email\\'")));
    }

    @Test
    @DisplayName("in getting an unknown user")
    void getSingleUser() throws Exception {
      mvc.perform(get("/users/{userIdentifier}", UUID.randomUUID()).with(user("user")))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("in deleting an unknown user")
    void deleteUser() throws Exception {
      mvc.perform(
              delete("/users/{userIdentifier}", UUID.randomUUID()).with(csrf()).with(user("user")))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("fails with unauthorized")
  class AuthenticationTests {

    @Test
    @DisplayName("in registering a new user")
    void registerUserUnAuthorized() throws Exception {
      CreateUserModel model =
          new CreateUserModel(
              "Hans",
              "Mustermann",
              "test@example.com",
              "MySecret4Test",
              Collections.singleton("USER"));
      mvc.perform(
              post("/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model))
                  .with(csrf()))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("in updating an existing user")
    void updateUserUnAuthorized() throws Exception {
      CreateUserModel model =
          new CreateUserModel(
              "Hans",
              "Mustermann",
              "test@example.com",
              "MySecret4Test",
              Collections.singleton("LIBRARY_ADMIN"));
      mvc.perform(
              put("/users/{userIdentifier}", DataInitializer.ADMIN_IDENTIFIER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model))
                  .with(csrf()))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("in getting a list of all users")
    void listAllUsersUnAuthorized() throws Exception {
      mvc.perform(get("/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("in getting a single user")
    void getSingleUserUnAuthorized() throws Exception {
      mvc.perform(get("/users/{userIdentifier}", DataInitializer.WAYNE_USER_IDENTIFIER))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("in deleting an user")
    void deleteUserUnAuthorized() throws Exception {
      mvc.perform(
              delete("/users/{userIdentifier}", DataInitializer.CURATOR_IDENTIFIER).with(csrf()))
          .andExpect(status().isUnauthorized());
    }
  }

  @DisplayName("fails for missing CSRF token")
  @Nested
  class CsrfTokenTests {

    @Test
    @DisplayName("in registering a new user")
    void registerUserNoCsrfToken() throws Exception {
      CreateUserModel model =
          new CreateUserModel(
              "Hans",
              "Mustermann",
              "test@example.com",
              "MySecret4Test",
              Collections.singleton("USER"));
      mvc.perform(
              post("/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model))
                  .with(user("user")))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("in updating an existing user")
    void updateUserNoCsrfToken() throws Exception {
      CreateUserModel model =
          new CreateUserModel(
              "Hans",
              "Mustermann",
              "test@example.com",
              "MySecret4Test",
              Collections.singleton("LIBRARY_ADMIN"));
      mvc.perform(
              put("/users/{userIdentifier}", DataInitializer.ADMIN_IDENTIFIER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model))
                  .with(user("user")))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("in deleting an user")
    void deleteUserNoCsrfToken() throws Exception {
      mvc.perform(
              delete("/users/{userIdentifier}", DataInitializer.CURATOR_IDENTIFIER)
                  .with(user("user")))
          .andExpect(status().isForbidden());
    }
  }
}
