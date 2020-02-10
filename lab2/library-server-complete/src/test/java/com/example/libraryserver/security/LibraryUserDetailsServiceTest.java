package com.example.libraryserver.security;

import com.example.libraryserver.user.data.User;
import com.example.libraryserver.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DisplayName("Verify UserDetailsService")
@ExtendWith(MockitoExtension.class)
class LibraryUserDetailsServiceTest {

  @Mock private UserService userService;

  @InjectMocks private LibraryUserDetailsService cut;

  @DisplayName("can load existing user")
  @Test
  void loadUserByUsername() {
    given(userService.findOneByEmail(any()))
        .willReturn(
            Optional.of(
                new User(
                    "Hans", "Test", "test@example.com", "secret", Collections.singleton("USER"))));
    UserDetails userDetails = cut.loadUserByUsername("test@example.com");
    assertThat(userDetails).isNotNull().isInstanceOf(AuthenticatedUser.class);
  }

  @DisplayName("reports expected error when user does not exist")
  @Test
  void loadUserByUsernameNotFound() {
    given(userService.findOneByEmail(any())).willReturn(Optional.empty());
    assertThatExceptionOfType(UsernameNotFoundException.class)
        .isThrownBy(() -> cut.loadUserByUsername("test@example.com"))
        .withMessage("No user found for test@example.com")
        .withNoCause();
  }

  @DisplayName("can update password for existing user")
  @Test
  void updatePassword() {
    User user =
        new User("Hans", "Test", "test@example.com", "secret", Collections.singleton("USER"));
    UserDetails userDetails = new AuthenticatedUser(user);

    given(userService.findOneByEmail(any())).willReturn(Optional.of(user));
    given(userService.save(any()))
        .willReturn(
            new User(
                "Hans", "Test", "test@example.com", "newpassword", Collections.singleton("USER")));

    UserDetails result = cut.updatePassword(userDetails, "newpassword");
    assertThat(result)
        .isNotNull()
        .isInstanceOf(AuthenticatedUser.class)
        .extracting(UserDetails::getPassword)
        .isEqualTo("newpassword");
  }

  @DisplayName("reports expected error when password could not be updated")
  @Test
  void updatePasswordUsernameNotFound() {
    User user =
        new User("Hans", "Test", "test@example.com", "secret", Collections.singleton("USER"));
    UserDetails userDetails = new AuthenticatedUser(user);

    given(userService.findOneByEmail(any())).willReturn(Optional.empty());
    assertThatExceptionOfType(UsernameNotFoundException.class)
        .isThrownBy(() -> cut.updatePassword(userDetails, "newpassword"))
        .withMessage("No user found for test@example.com")
        .withNoCause();
  }
}
