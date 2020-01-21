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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
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
@DisplayName("Verify user rest api")
class UserRestControllerIntegrationTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

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
            .defaultRequest(post("/").with(csrf()).with(user("user").roles("USER")))
            .build();
  }

  @Nested
  @DisplayName("succeeds")
  class PositiveTests {

    @Test
    @DisplayName("in registering a new user")
    void registerUser() throws Exception {
      UserModel model =
          new UserModel(
              "Hans", "Mustermann", "test@example.com", "password", Collections.singleton("USER"));
      mvc.perform(
              post("/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model)))
          .andExpect(status().isCreated())
          .andExpect(header().exists("location"))
          .andExpect(jsonPath("$.identifier").exists())
          .andDo(document("create-user"));
    }

    @Test
    @DisplayName("in updating an existing user")
    void updateUser() throws Exception {
      UserModel model =
          new UserModel(
              "Hans",
              "Mustermann",
              "test@example.com",
              "password",
              Collections.singleton("LIBRARY_ADMIN"));
      mvc.perform(
              put("/users/{userIdentifier}", DataInitializer.ADMIN_IDENTIFIER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.lastName").value("Mustermann"))
          .andDo(document("update-user"));
    }

    @Test
    @DisplayName("in getting a list of all users")
    void listAllUsers() throws Exception {
      mvc.perform(get("/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.users.length()").value(greaterThan(0)))
          .andDo(document("get-users"));
    }

    @Test
    @DisplayName("in getting a single user")
    void getSingleUser() throws Exception {
      mvc.perform(get("/users/{userIdentifier}", DataInitializer.WAYNE_USER_IDENTIFIER))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.identifier").value(DataInitializer.WAYNE_USER_IDENTIFIER.toString()))
          .andExpect(jsonPath("$.lastName").value("Wayne"))
          .andDo(document("get-user"));
    }

    @Test
    @DisplayName("in deleting an user")
    void deleteUser() throws Exception {
      mvc.perform(delete("/users/{userIdentifier}", DataInitializer.CURATOR_IDENTIFIER))
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
      UserModel model =
          new UserModel(
              "Hans", "Mustermann", "example.com", "password", Collections.singleton("USER"));
      mvc.perform(
              post("/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model)))
          .andExpect(status().isBadRequest())
          .andExpect(
              content()
                  .string(
                      startsWith("Field error in object \\'userModel\\' on field \\'email\\'")));
    }

    @Test
    @DisplayName("in updating an existing user with invalid email")
    void updateUser() throws Exception {
      UserModel model =
          new UserModel(
              "Hans",
              "Mustermann",
              "example.com",
              "password",
              Collections.singleton("LIBRARY_ADMIN"));
      mvc.perform(
              put("/users/{userIdentifier}", DataInitializer.ADMIN_IDENTIFIER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(model)))
          .andExpect(status().isBadRequest())
          .andExpect(
              content()
                  .string(
                      startsWith("Field error in object \\'userModel\\' on field \\'email\\'")));
    }

    @Test
    @DisplayName("in getting an unknown user")
    void getSingleUser() throws Exception {
      mvc.perform(get("/users/{userIdentifier}", UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("in deleting an unknown user")
    void deleteUser() throws Exception {
      mvc.perform(delete("/users/{userIdentifier}", UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }
  }
}
