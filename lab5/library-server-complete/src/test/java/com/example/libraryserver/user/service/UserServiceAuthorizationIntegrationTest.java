package com.example.libraryserver.user.service;

import com.example.libraryserver.config.IdGeneratorConfiguration;
import com.example.libraryserver.security.AuthenticatedUser;
import com.example.libraryserver.user.data.User;
import com.example.libraryserver.user.data.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("User service")
@SpringJUnitConfig(classes = UserServiceAuthorizationIntegrationTest.BookServiceConfig.class)
class UserServiceAuthorizationIntegrationTest {

  @Autowired private UserService cut;

  @SuppressWarnings("unused")
  @MockBean
  private UserRepository userRepository;

  private void createAuthenticationContext(String... roles) {
    TestSecurityContextHolder.setAuthentication(
        new TestingAuthenticationToken(
            new AuthenticatedUser(
                new User(
                    UUID.randomUUID(),
                    "Hans",
                    "Test",
                    "test@example.com",
                    "secret",
                    Collections.emptySet())),
            "secret",
            AuthorityUtils.createAuthorityList(roles)));
  }

  @TestConfiguration
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  @Import({UserService.class, IdGeneratorConfiguration.class})
  static class BookServiceConfig {}

  @DisplayName("is authorized for")
  @WithMockUser(roles = "LIBRARY_ADMIN")
  @Nested
  class PositiveAuthorizationTests {

    @DisplayName("finding a user by identifier")
    @Test
    void findOneByIdentifier() {
      cut.findOneByIdentifier(UUID.randomUUID());
    }

    @DisplayName("finding a user by email")
    @WithAnonymousUser
    @Test
    void findOneByEmail() {
      cut.findOneByEmail("test@example.com");
    }

    @DisplayName("finding all books")
    @Test
    void findAll() {
      cut.findAll();
    }

    @DisplayName("saving a book")
    @Test
    void save() {
      cut.save(
          new User("Hans", "Test", "test@example.com", "secret", Collections.singleton("USER")));
    }

    @DisplayName("deleting a book")
    @Test
    void deleteOneIdentifier() {
      cut.deleteOneIdentifier(UUID.randomUUID());
    }
  }

  @DisplayName("is not authorized for")
  @Nested
  class NegativeAuthorizationTests {

    @DisplayName("finding a book by identifier with")
    @ParameterizedTest
    @ValueSource(strings = {"ROLE_LIBRARY_USER", "ROLE_LIBRARY_CURATOR", "ROLE_USER"})
    void findOneByIdentifier(String role) {
      createAuthenticationContext(role);
      assertThatExceptionOfType(AccessDeniedException.class)
          .isThrownBy(() -> cut.findOneByIdentifier(UUID.randomUUID()))
          .withMessage("Access is denied");
    }

    @DisplayName("finding all books with")
    @ParameterizedTest
    @ValueSource(strings = {"ROLE_LIBRARY_USER", "ROLE_LIBRARY_CURATOR", "ROLE_USER"})
    void findAll(String role) {
      createAuthenticationContext(role);
      assertThatExceptionOfType(AccessDeniedException.class)
          .isThrownBy(() -> cut.findAll())
          .withMessage("Access is denied");
    }

    @DisplayName("saving a book with")
    @ParameterizedTest
    @ValueSource(strings = {"ROLE_LIBRARY_USER", "ROLE_LIBRARY_CURATOR", "ROLE_USER"})
    void save(String role) {
      createAuthenticationContext(role);
      assertThatExceptionOfType(AccessDeniedException.class)
          .isThrownBy(
              () ->
                  cut.save(
                      new User(
                          "Hans",
                          "Test",
                          "test@example.com",
                          "secret",
                          Collections.singleton("USER"))))
          .withMessage("Access is denied");
    }

    @DisplayName("deleting a book with")
    @ParameterizedTest
    @ValueSource(strings = {"ROLE_LIBRARY_USER", "ROLE_LIBRARY_CURATOR", "ROLE_USER"})
    void deleteOneIdentifier(String role) {
      createAuthenticationContext(role);
      assertThatExceptionOfType(AccessDeniedException.class)
          .isThrownBy(() -> cut.deleteOneIdentifier(UUID.randomUUID()))
          .withMessage("Access is denied");
    }
  }
}
