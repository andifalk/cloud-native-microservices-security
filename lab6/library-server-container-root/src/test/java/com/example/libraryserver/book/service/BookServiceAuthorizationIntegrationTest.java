package com.example.libraryserver.book.service;

import com.example.libraryserver.book.data.Book;
import com.example.libraryserver.book.data.BookRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Book service")
@SpringJUnitConfig(classes = BookServiceAuthorizationIntegrationTest.BookServiceConfig.class)
class BookServiceAuthorizationIntegrationTest {

  @Autowired private BookService cut;
  @MockBean private BookRepository bookRepository;
  @MockBean private UserRepository userRepository;

  private AuthenticatedUser getPrincipal() {
    return (AuthenticatedUser)
        TestSecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  private void createAuthenticationContext(User expectedUser) {
    TestSecurityContextHolder.setAuthentication(
        new UsernamePasswordAuthenticationToken(
            new AuthenticatedUser(expectedUser),
            "secret",
            AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_LIBRARY_USER")));
  }

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
  @Import({BookService.class, IdGeneratorConfiguration.class})
  static class BookServiceConfig {}

  @DisplayName("is authorized for")
  @WithMockUser
  @Nested
  class PositiveAuthorizationTests {

    @DisplayName("finding book by identifier")
    @Test
    void findOneByIdentifier() {
      cut.findOneByIdentifier(UUID.randomUUID());
    }

    @DisplayName("finding all books")
    @Test
    void findAll() {
      cut.findAll();
    }

    @DisplayName("saving a book")
    @WithMockUser(roles = "LIBRARY_CURATOR")
    @Test
    void verifySave() {
      cut.save(new Book("1234567890", "title", "desc", Collections.singleton("author")));
    }

    @DisplayName("borrowing a book")
    @Test
    void borrowForUser() {
      createAuthenticationContext("ROLE_LIBRARY_USER");
      cut.borrowForUser(UUID.randomUUID(), UUID.randomUUID(), getPrincipal());
    }

    @DisplayName("returning a borrowed book")
    @Test
    void returnForUser() {
      createAuthenticationContext("ROLE_LIBRARY_USER");
      cut.returnForUser(UUID.randomUUID(), UUID.randomUUID(), getPrincipal());
    }

    @DisplayName("deleting a book")
    @WithMockUser(roles = "LIBRARY_CURATOR")
    @Test
    void deleteOneByIdentifier() {
      cut.deleteOneByIdentifier(UUID.randomUUID());
    }
  }

  @DisplayName("is not authorized for")
  @Nested
  class NegativeAuthorizationTests {

    @DisplayName("saving a book with")
    @ParameterizedTest
    @ValueSource(strings = {"ROLE_LIBRARY_USER", "ROLE_LIBRARY_ADMIN", "ROLE_USER"})
    void verifySave(String role) {
      createAuthenticationContext(role);
      assertThatExceptionOfType(AccessDeniedException.class)
          .isThrownBy(
              () ->
                  cut.save(
                      new Book("1234567890", "title", "desc", Collections.singleton("author"))));
    }

    @DisplayName("borrowing a book with")
    @ParameterizedTest
    @ValueSource(strings = {"ROLE_LIBRARY_CURATOR", "ROLE_LIBRARY_ADMIN", "ROLE_USER"})
    void borrowForUser(String role) {
      createAuthenticationContext(role);
      assertThatExceptionOfType(AccessDeniedException.class)
          .isThrownBy(
              () -> cut.borrowForUser(UUID.randomUUID(), UUID.randomUUID(), getPrincipal()));
    }

    @DisplayName("returning a book with")
    @ParameterizedTest
    @ValueSource(strings = {"ROLE_LIBRARY_CURATOR", "ROLE_LIBRARY_ADMIN", "ROLE_USER"})
    void returnForUser(String role) {
      createAuthenticationContext(role);
      assertThatExceptionOfType(AccessDeniedException.class)
          .isThrownBy(
              () -> cut.returnForUser(UUID.randomUUID(), UUID.randomUUID(), getPrincipal()));
    }

    @DisplayName("deleting a book with")
    @ParameterizedTest
    @ValueSource(strings = {"ROLE_LIBRARY_USER", "ROLE_LIBRARY_ADMIN", "ROLE_USER"})
    void deleteOneByIdentifier(String role) {
      createAuthenticationContext(role);
      assertThatExceptionOfType(AccessDeniedException.class)
          .isThrownBy(() -> cut.deleteOneByIdentifier(UUID.randomUUID()));
    }
  }
}
