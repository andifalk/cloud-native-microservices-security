package com.example.libraryserver.security;

import com.example.libraryserver.config.PasswordValidationConfiguration;
import com.example.libraryserver.user.service.InvalidPasswordError;
import com.example.libraryserver.user.service.PasswordValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@DisplayName("Password validation")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PasswordValidationConfiguration.class)
public class PasswordValidationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordValidationTest.class);

  private final PasswordValidationService passwordValidationService;

  @Autowired
  public PasswordValidationTest(PasswordValidationService passwordValidationService) {
    this.passwordValidationService = passwordValidationService;
  }

  @Test
  @DisplayName("succeeds for valid password")
  void verifyValidPassword() {
    passwordValidationService.validate("user", "my!Secret4test");
  }

  @Test
  @DisplayName("succeeds for valid password with unicode character")
  void verifyValidPasswordWithUnicode() {
    String unicodePassword = "my!Secret4öest" + "\uD83D\uDE02\uD83D\uDE0D\uD83C\uDF89\uD83D\uDC4D";
    LOGGER.info("Password: {}", unicodePassword);
    passwordValidationService.validate("user", unicodePassword);
  }

  @Test
  @DisplayName("fails for too short password")
  void verifyInvalidPasswordTooShort() {
    InvalidPasswordError error =
        catchThrowableOfType(
            () -> passwordValidationService.validate("user", "my!Sec4test"),
            InvalidPasswordError.class);

    assertThat(error).isNotNull();

    List<String> messages = error.getValidationErrors();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0)).isEqualTo("Password must be 12 or more characters in length.");
  }

  @Test
  @DisplayName("fails for too long password")
  void verifyInvalidPasswordTooLong() {

    InvalidPasswordError error =
            catchThrowableOfType(
                    () -> passwordValidationService.validate(
                            "user", "my!Sec4testedfrewasdefvbnhjlkilomngthfargtwbnhjlmnhsömnöämnhjqpolwk"),
                    InvalidPasswordError.class);

    assertThat(error).isNotNull();
    List<String> messages = error.getValidationErrors();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0)).isEqualTo("Password must be no more than 64 characters in length.");
  }

  @Test
  @DisplayName("fails for too many repeating characters")
  void verifyInvalidPasswordTooManyRepeatingChars() {

    InvalidPasswordError error =
            catchThrowableOfType(
                    () -> passwordValidationService.validate("user", "my!Sec4teeeest"),
                    InvalidPasswordError.class);

    assertThat(error).isNotNull();


    List<String> messages = error.getValidationErrors();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0)).isEqualTo("Password matches the illegal pattern 'eeee'.");
  }

  @Test
  @DisplayName("fails for username in password")
  void verifyInvalidPasswordUsernameInPassword() {

    InvalidPasswordError error =
            catchThrowableOfType(
                    () -> passwordValidationService.validate("user", "my!Secret4tuser"),
                    InvalidPasswordError.class);

    assertThat(error).isNotNull();

    List<String> messages = error.getValidationErrors();
    assertThat(messages.size()).isEqualTo(1);
    assertThat(messages.get(0)).isEqualTo("Password contains the user id 'user'.");
  }

  @Test
  @DisplayName("fails with expected multiple validation errors")
  void verifyInvalidPasswordMultipleErrors() {

    InvalidPasswordError error =
            catchThrowableOfType(
                    () -> passwordValidationService.validate("user", "qwertyuser"),
                    InvalidPasswordError.class);

    assertThat(error).isNotNull();

    List<String> messages = error.getValidationErrors();
    assertThat(messages.size()).isEqualTo(7);
    assertThat(messages)
        .containsExactlyInAnyOrder(
            "Password must be 12 or more characters in length.",
            "Password must contain 1 or more uppercase characters.",
            "Password must contain 1 or more digit characters.",
            "Password must contain 1 or more special characters.",
            "Password matches 1 of 4 character rules, but 3 are required.",
            "Password contains the user id 'user'.",
            "Password contains the dictionary word 'qwerty'.");
  }
}
