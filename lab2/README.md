# Lab 2: Customized Authentication

Now it is time to start customizing the auto-configuration.

As soon as you customize any bit of Spring Security the complete spring boot auto-configuration will back-off.

## Custom authentication with persistent users

Before we start let's look into some internal details how spring security works
for the servlet web stack.

By using a _ServletFilter_ you can add functionality that is called around each request and response.
Spring Security provides several web filter out of the box.

| Filter | Description           |
| ---------| ------------------------ |
| AuthenticationWebFilter   | Performs authentication of a particular request  |
| AuthorizationWebFilter  | Determines if an authenticated user has access to a specific object   |
| CorsWebFilter  | Handles CORS preflight requests and intercepts  |
| CsrfWebFilter    | Applies CSRF protection using a synchronizer token pattern.   |
_Spring Security WebFilter_

## Security Filter Chain

Spring Security configures security by utilizing a _Security Filter Chain_

In step 1 we just used the auto configuration of Spring Boot.
This configured a default security filter chain.

As part of this lab we will customize several things for authentication:

* Connect the existing persistent user data with Spring Security to enable authentication based on these
* Encode the password values to secure hashed ones in the database
* Ensure a password policy to enforce secure passwords (a common source of hacking authentication are weak passwords)

## Encrypting Passwords

We start by replacing the default user/password with our own persistent user storage (already present in DB).
To do this we add a new class _WebSecurityConfiguration_ to package _com.example.libraryserver.config_ having the following
contents.

```java
package com.example.libraryserver.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

  @Primary
  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Qualifier("LegacyEncoder")
  @Bean
  public PasswordEncoder legacyPasswordEncoder() {
    String encodingId = "MD5";
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put(
        encodingId,
        new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
    return new DelegatingPasswordEncoder(encodingId, encoders);
  }

  @Configuration
  public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      // http.csrf().disable();
      http.authorizeRequests(
              authorizeRequests ->
                  authorizeRequests
                      .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
                      .permitAll()
                      .requestMatchers(EndpointRequest.toAnyEndpoint())
                      .authenticated()
                      .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                      .permitAll()
                      .mvcMatchers("/")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .httpBasic(withDefaults())
          .formLogin(withDefaults());
    }
  }
}
```
_WebSecurityConfiguration.java_

The _WebSecurityConfiguration_ implementation does two important things:

* This adds the [SecurityWebFilterChain](https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#security-filter-chain)
* Configures a _PasswordEncoder_. A password encoder is used by spring security to encrypt passwords and to check if a given password matches the encrypted one.
  You may recognize as well a legacy password encoder (this will be used later in this lab for password upgrades)

```java
package org.springframework.security.crypto.password;

public interface PasswordEncoder {

	String encode(CharSequence rawPassword); // <1>

	boolean matches(CharSequence rawPassword, String encodedPassword); // <2>
}
```
_PasswordEncoder interface_

<1> Encrypts the given cleartext password
<2> Validates the given cleartext password with the encrypted one (without revealing the unencrypted one)

In spring security 5 creating an instance of the _DelegatingPasswordEncoder_ is much easier
by using the class _PasswordEncoderFactories_. In past years several previously used password encryption algorithms
have been broken (like _MD4_ or _MD5_). By using _PasswordEncoderFactories_ you always get a configured
_PasswordEncoder_ that uses an _PasswordEncoder_ with a _state of the art_ encryption algorithm like _BCrypt_ or _Argon2_ at the time
of creating this workshop.

```java
package org.springframework.security.crypto.factory;

public class PasswordEncoderFactories {

	public static PasswordEncoder createDelegatingPasswordEncoder() {
		String encodingId = "bcrypt";
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put(encodingId, new BCryptPasswordEncoder());
		encoders.put("ldap", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
		encoders.put("MD4", new org.springframework.security.crypto.password.Md4PasswordEncoder());
		encoders.put("MD5", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
		encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
		encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
		encoders.put("scrypt", new SCryptPasswordEncoder());
		encoders.put("SHA-1", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
		encoders.put("SHA-256", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256"));
		encoders.put("sha256", new org.springframework.security.crypto.password.StandardPasswordEncoder());
		encoders.put("argon2", new Argon2PasswordEncoder());

		return new DelegatingPasswordEncoder(encodingId, encoders);
	}

	private PasswordEncoderFactories() {}
}
```
_DelegatingPasswordEncoder_

To have encrypted passwords in our database we need to tweak our existing _DataInitializer_ a bit with the
_PasswordEncoder_ we just have configured.

```java
package com.example.libraryserver;

import com.example.libraryserver.book.data.Book;
import com.example.libraryserver.book.data.BookRepository;
import com.example.libraryserver.user.data.User;
import com.example.libraryserver.user.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DataInitializer implements CommandLineRunner {

  //...
  private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordEncoder legacyPasswordEncoder;

  public DataInitializer(
      BookRepository bookRepository,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Qualifier("LegacyEncoder") PasswordEncoder legacyPasswordEncoder) {
    this.bookRepository = bookRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.legacyPasswordEncoder = legacyPasswordEncoder;
  }

  @Override
  public void run(String... args) {
    createUsers();
    createBooks();
  }

  @Transactional
  void createUsers() {

    LOGGER.info("Creating users with LIBRARY_USER, LIBRARY_CURATOR and LIBRARY_ADMIN roles...");
    List<User> userList =
        Stream.of(
                new User(
                    LEGACY_USER_IDENTIFIER,
                    "Doctor",
                    "Strange",
                    "doctor.strange@example.com",
                    legacyPasswordEncoder.encode("strange"),
                    Collections.singleton("LIBRARY_USER")),
                new User(
                    WAYNE_USER_IDENTIFIER,
                    "Bruce",
                    "Wayne",
                    "bruce.wayne@example.com",
                    passwordEncoder.encode("wayne"),
                    Collections.singleton("LIBRARY_USER")),
                new User(
                    BANNER_USER_IDENTIFIER,
                    "Bruce",
                    "Banner",
                    "bruce.banner@example.com",
                    passwordEncoder.encode("banner"),
                    Collections.singleton("LIBRARY_USER")),
                new User(
                    CURATOR_IDENTIFIER,
                    "Peter",
                    "Parker",
                    "peter.parker@example.com",
                    passwordEncoder.encode("parker"),
                    Collections.singleton("LIBRARY_CURATOR")),
                new User(
                    ADMIN_IDENTIFIER,
                    "Clark",
                    "Kent",
                    "clark.kent@example.com",
                    passwordEncoder.encode("kent"),
                    Collections.singleton("LIBRARY_ADMIN")))
            .map(userRepository::save)
            .collect(Collectors.toList());
    LOGGER.info("Created {} users", userList.size());
  }

  @Transactional
  void createBooks() {
    //...
  }
}
```
_DataInitializer.java_

## Persistent User Storage

Now that we already have configured the encrypting part for passwords of our user storage
we need to connect our own user store (the users already stored in the DB) with spring security's
authentication manager.

This is done in two steps:

In the first step we need to implement spring security's definition of a user implementing _UserDetails_.
Please create a new class called _AuthenticatedUser_ in package _com.example.libraryserver.security_.

To make it a bit easier we just extend our existing _User_ data class.


```java
package com.example.libraryserver.security;

import com.example.libraryserver.user.data.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class AuthenticatedUser extends User implements UserDetails {

  public AuthenticatedUser(User user) {
    super(
        user.getIdentifier(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getPassword(),
        user.getRoles());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return AuthorityUtils.commaSeparatedStringToAuthorityList(String.join(",", getRoles()));
  }

  @Override
  public String getUsername() {
    return getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
```
_AuthenticatedUser.java_

In the second step we need to implement spring security's interface _UserDetailsService_ to integrate our user store with the authentication manager.
Please go ahead and create a new class _LibraryUserDetailsService_ in package _com.example.libraryserver.security_:

```java
package com.example.libraryserver.security;

import com.example.libraryserver.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Qualifier("library-user-details-service")
@Service
public class LibraryUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LibraryUserDetailsService.class);

  private final UserService userService;

  public LibraryUserDetailsService(UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userService
        .findOneByEmail(username)
        .map(AuthenticatedUser::new)
        .orElseThrow(() -> new UsernameNotFoundException("No user found for " + username));
  }

  @Override
  public UserDetails updatePassword(UserDetails user, String newPassword) {
    return userService
        .findOneByEmail(user.getUsername())
        .map(
            u -> {
              LOGGER.info(
                  "Upgrading password {} for user {} to {}",
                  user.getPassword(),
                  user.getUsername(),
                  newPassword);
              u.setPassword(newPassword);
              return new AuthenticatedUser(userService.save(u));
            })
        .orElseThrow(
            () -> new UsernameNotFoundException("No user found for " + user.getUsername()));
  }
}
```
_LibraryUserDetailsService.java_

After completing this part of the workshop we now still have the auto-configured _SecurityWebFilterChain_ but we have
replaced the default user with our own users from our DB persistent storage.

If you restart the application now you have to use the following user credentials to log in:

| Username | Email                    | Password | Role            |
| ---------| ------------------------ | -------- | --------------- |
| bwayne   | bruce.wayne@example.com  | wayne    | LIBRARY_USER    |
| bbanner  | bruce.banner@example.com | banner   | LIBRARY_USER    |
| pparker  | peter.parker@example.com | parker   | LIBRARY_CURATOR |
| ckent    | clark.kent@example.com   | kent     | LIBRARY_ADMIN   |

### Authenticated Principal

As we now have a persistent authenticated user we can now also use this user to check if the current user is allowed
to borrow or return a book. This requires changes in _BookService_ and _BookRestController_.

First change the class _BookService_:

```java
package com.example.libraryserver.book.service;

import com.example.libraryserver.book.data.Book;
import com.example.libraryserver.book.data.BookRepository;
import com.example.libraryserver.security.AuthenticatedUser;
import com.example.libraryserver.user.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BookService.class);

  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final IdGenerator idGenerator;

  public BookService(
      BookRepository bookRepository, UserRepository userRepository, IdGenerator idGenerator) {
    this.bookRepository = bookRepository;
    this.userRepository = userRepository;
    this.idGenerator = idGenerator;
  }

  // ...

  @Transactional
  public Optional<Book> borrowForUser(
      UUID bookIdentifier, UUID userIdentifier, AuthenticatedUser authenticatedUser) {
    LOGGER.trace(
        "borrow book with identifier {} for user with identifier {}",
        bookIdentifier,
        userIdentifier);

    return bookRepository
        .findOneByIdentifier(bookIdentifier)
        .filter(
            b ->
                b.getBorrowedByUser() == null
                    && authenticatedUser != null
                    && userIdentifier.equals(authenticatedUser.getIdentifier()))
        .flatMap(
            b ->
                userRepository
                    .findOneByIdentifier(userIdentifier)
                    .map(
                        u -> {
                          b.setBorrowedByUser(u);
                          Book borrowedBook = bookRepository.save(b);
                          LOGGER.info("Borrowed book {} for user {}", borrowedBook, u);
                          return Optional.of(borrowedBook);
                        })
                    .orElse(Optional.empty()));
  }

  @Transactional
  public Optional<Book> returnForUser(
      UUID bookIdentifier, UUID userIdentifier, AuthenticatedUser authenticatedUser) {
    LOGGER.trace(
        "return book with identifier {} of user with identifier {}",
        bookIdentifier,
        userIdentifier);

    return bookRepository
        .findOneByIdentifier(bookIdentifier)
        .filter(
            b ->
                b.getBorrowedByUser() != null
                    && authenticatedUser != null
                    && b.getBorrowedByUser().getIdentifier().equals(userIdentifier)
                    && b.getBorrowedByUser()
                        .getIdentifier()
                        .equals(authenticatedUser.getIdentifier()))
        .flatMap(
            b ->
                userRepository
                    .findOneByIdentifier(userIdentifier)
                    .map(
                        u -> {
                          b.setBorrowedByUser(null);
                          Book returnedBook = bookRepository.save(b);
                          LOGGER.info("Returned book {} for user {}", returnedBook, u);
                          return Optional.of(returnedBook);
                        })
                    .orElse(Optional.empty()));
  }

  //...

}
```
_BookService.java_

Then please adapt the _BookRestController_ accordingly.

```java
package com.example.libraryserver.book.web;

import com.example.libraryserver.book.data.Book;
import com.example.libraryserver.book.service.BookService;
import com.example.libraryserver.security.AuthenticatedUser;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/books")
@Validated
public class BookRestController {

  private final BookService bookService;
  private final BookModelAssembler bookModelAssembler;

  public BookRestController(BookService bookService, BookModelAssembler bookModelAssembler) {
    this.bookService = bookService;
    this.bookModelAssembler = bookModelAssembler;
  }

  // ...

  @PostMapping("/{bookIdentifier}/borrow/{userIdentifier}")
  public ResponseEntity<BookModel> borrowBook(
      @PathVariable("bookIdentifier") UUID bookIdentifier,
      @PathVariable("userIdentifier") UUID userIdentifier,
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

    return bookService
        .borrowForUser(bookIdentifier, userIdentifier, authenticatedUser)
        .map(b -> ResponseEntity.ok(bookModelAssembler.toModel(b)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{bookIdentifier}/return/{userIdentifier}")
  public ResponseEntity<BookModel> returnBook(
      @PathVariable("bookIdentifier") UUID bookIdentifier,
      @PathVariable("userIdentifier") UUID userIdentifier,
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

    return bookService
        .returnForUser(bookIdentifier, userIdentifier, authenticatedUser)
        .map(b -> ResponseEntity.ok(bookModelAssembler.toModel(b)))
        .orElse(ResponseEntity.notFound().build());
  }

  // ...
}
```
_BookRestController.java_

You will get compilation errors in class _BookModelAssembler_.
Here you just have to adapt the method calls for _returnBook_ and _borrowBook_.

```java
package com.example.libraryserver.book.web;

import com.example.libraryserver.book.data.Book;
import com.example.libraryserver.user.web.UserModelAssembler;
import org.owasp.encoder.Encode;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookModelAssembler extends RepresentationModelAssemblerSupport<Book, BookModel> {

  public BookModelAssembler() {
    super(BookRestController.class, BookModel.class);
  }

  @Override
  public BookModel toModel(Book book) {
    BookModel bookModel =
        outputEscaping(
            new BookModel(
                book.getIdentifier(),
                book.getIsbn(),
                book.getTitle(),
                book.getDescription(),
                book.getAuthors(),
                book.getBorrowedByUser() != null
                    ? new UserModelAssembler().toModel(book.getBorrowedByUser())
                    : null));
    bookModel.add(
        linkTo(methodOn(BookRestController.class).getSingleBook(bookModel.getIdentifier()))
            .withSelfRel());
    bookModel.add(
        linkTo(methodOn(BookRestController.class).borrowBook(bookModel.getIdentifier(), null, null))
            .withRel("borrow"));
    bookModel.add(
        linkTo(methodOn(BookRestController.class).returnBook(bookModel.getIdentifier(), null, null))
            .withRel("return"));

    return bookModel;
  }
  
  // ...
}
```
_BookModelAssembler.java_

## Automatic Password Encryption Updates

We already looked into the _DelegatingPasswordEncoder_ and _PasswordEncoderFactories_. As these classes have knowledge
about all encryption algorithms that are supported in spring security, the framework can detect
an _outdated_ encryption algorithm. If you look at the _LibraryUserDetailsService_ class we just have added
this also implements the additionally provided interface _UserDetailsPasswordService_. This way we can now enable an automatic
password encryption upgrade mechanism.

The _UserDetailsPasswordService_ interface just defines one more operation.

```java
package org.springframework.security.core.userdetails;

public interface UserDetailsPasswordService {

	UserDetails updatePassword(UserDetails user, String newPassword);
}
```
_UserDetailsPasswordService interface_

We already have a user using a password that is encrypted using an _outdated_ _MD5_ algorithm. We achieved this by defining a legacy user
_doctor.strange@example.com_ with password _strange_ in the existing _DataInitializer_ class.

Now restart the application and see what happens if we try to get the list of books using this new
user (username='doctor.strange@example.com', password='strange').

In the console you should see the log output showing the old _MD5_ password being updated to _bcrypt_ password.

__CAUTION:__ Never log any sensitive data like passwords, tokens etc., even in encrypted format. Also never put such sensitive data
into your version control. And never let error details reach the client (via REST API or web application).
Make sure you disable stacktraces in client error messages using property _server.error.include-stacktrace=never_


## Actuator Security

It is also a good idea to restrict the details of the health actuator endpoint to authenticated users.
Anonymous users should only see the _UP_ or _DOWN_ status values but no further details.

Just change this entry in _application.yml_ file:

```yaml
...
management:
  endpoint:
    health:
      show-details: when_authorized
...
```

## Adding a Password Policy

Usually not the authentication mechanisms are hacked, instead weak passwords are the most critical source for attacking authentication.
Therefore strong passwords are really important. It is also not a good practice any more to force users to change their passwords after
a period of time.

Now we want to follow the recommendations of the NIST for secure passwords: [NIST (section 5.1.1.2 Memorized Secret Verifiers)](https://pages.nist.gov/800-63-3/sp800-63b.html) 
to implement a password policy.

We will utilize the [Passay](https://www.passay.org) library for this.

So first add a new dependency to _build.gradle_:

```groovy
dependencies {
    // ...
	implementation 'org.passay:passay:1.5.0'
    // ...
}
``` 


```java
package com.example.libraryserver.user.service;

import org.passay.CharacterCharacteristicsRule;
import org.passay.CharacterRule;
import org.passay.DictionarySubstringRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RepeatCharacterRegexRule;
import org.passay.RuleResult;
import org.passay.UsernameRule;
import org.passay.WhitespaceRule;
import org.passay.dictionary.ArrayWordList;
import org.passay.dictionary.WordList;
import org.passay.dictionary.WordListDictionary;
import org.passay.dictionary.WordLists;
import org.passay.dictionary.sort.ArraysSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Password policy validator. Uses recommendations from
 * https://pages.nist.gov/800-63-3/sp800-63b.html (section 5.1.1.2 Memorized Secret Verifiers)
 */
public class PasswordValidationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordValidationService.class);

  /* https://github.com/danielmiessler/SecLists/blob/master/Passwords/darkweb2017-top100.txt */
  private static final String PASSWORD_LIST_TXT = "password-list.txt";

  private PasswordValidator passwordValidator;

  @PostConstruct
  public void init() {

    WordList wordList;
    try {
      ClassPathResource resource = new ClassPathResource(PASSWORD_LIST_TXT);
      wordList =
          WordLists.createFromReader(
              new FileReader[] {new FileReader(resource.getFile())}, false, new ArraysSort());
      LOGGER.info(
          "Successfully loaded the password list from {} with size {}",
          resource.getURL(),
          wordList.size());
    } catch (IOException ex) {
      wordList =
          new ArrayWordList(
              new String[] {
                "password", "Password", "123456", "12345678", "admin", "geheim", "secret"
              },
              false,
              new ArraysSort());
      LOGGER.warn("Error loading the password list: {}", ex.getMessage());
    }

    CharacterCharacteristicsRule characteristicsRule = new CharacterCharacteristicsRule();

    characteristicsRule.setNumberOfCharacteristics(3);

    characteristicsRule.getRules().add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
    characteristicsRule.getRules().add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
    characteristicsRule.getRules().add(new CharacterRule(EnglishCharacterData.Digit, 1));
    characteristicsRule.getRules().add(new CharacterRule(EnglishCharacterData.Special, 1));

    this.passwordValidator =
        new PasswordValidator(
            Arrays.asList(
                new LengthRule(12, 64),
                characteristicsRule,
                new RepeatCharacterRegexRule(4),
                new UsernameRule(),
                new WhitespaceRule(),
                new DictionarySubstringRule(new WordListDictionary(wordList))));
  }

  public void validate(String username, String password) {
    RuleResult result = this.passwordValidator.validate(new PasswordData(username, password));
    if (!result.isValid()) {
      List<String> messages = passwordValidator.getMessages(result);
      LOGGER.warn("Password validation failed");
      messages.forEach(LOGGER::info);
      throw new InvalidPasswordError(messages);
    } else {
      LOGGER.info("Password validated successfully");
    }
  }
}
```
_PasswordValidationService.java_

We also need to configure this service as Spring bean by creating new class _PasswordValidationConfiguration_:

```java
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
```

We also check for well-known insecure passwords using a password list.
There are plenty of password lists available on the internet (especially useful for performing brute force attacks by hackers).
Please download one of these the password list from [https://github.com/danielmiessler/SecLists/blob/master/Passwords/darkweb2017-top100.txt](https://github.com/danielmiessler/SecLists/blob/master/Passwords/darkweb2017-top100.txt) 
and store this file as _password-list.txt_ in folder _src/main/resources_.

We also need a special error for reporting password policy violations. Please create a new class _InvalidPasswordError_.

```java
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
```

Now we have to add the _PasswordValidationService_ to our _UserRestController_ to check the policy when creating or updating
a _User_.

```java
package com.example.libraryserver.user.web;

import com.example.libraryserver.user.data.User;
import com.example.libraryserver.user.service.PasswordValidationService;
import com.example.libraryserver.user.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/users")
public class UserRestController {

  private final UserService userService;
  private final PasswordValidationService passwordValidationService;
  private final UserModelAssembler userModelAssembler;

  public UserRestController(
      UserService userService,
      PasswordValidationService passwordValidationService,
      UserModelAssembler userModelAssembler) {
    this.userService = userService;
    this.passwordValidationService = passwordValidationService;
    this.userModelAssembler = userModelAssembler;
  }

  @PostMapping
  public ResponseEntity<UserModel> registerUser(
      @RequestBody @Valid CreateUserModel createUserModel, HttpServletRequest request) {

    passwordValidationService.validate(createUserModel.getEmail(), createUserModel.getPassword());

    User user =
        userService.save(
            new User(
                createUserModel.getFirstName(),
                createUserModel.getLastName(),
                createUserModel.getEmail(),
                createUserModel.getPassword(),
                createUserModel.getRoles()));
    URI uri =
        ServletUriComponentsBuilder.fromServletMapping(request)
            .path("/users/" + user.getIdentifier())
            .build()
            .toUri();

    return ResponseEntity.created(uri).body(userModelAssembler.toModel(user));
  }

  @PutMapping("/{userIdentifier}")
  public ResponseEntity<UserModel> updateUser(
      @PathVariable("userIdentifier") UUID userIdentifier,
      @RequestBody @Valid CreateUserModel createUserModel) {

    return userService
        .findOneByIdentifier(userIdentifier)
        .map(
            u -> {
              if (!u.getPassword().equals(createUserModel.getPassword())) {
                passwordValidationService.validate(
                    createUserModel.getEmail(), createUserModel.getPassword());
              }
              u.setFirstName(createUserModel.getFirstName());
              u.setLastName(createUserModel.getLastName());
              u.setEmail(createUserModel.getEmail());
              u.setPassword(createUserModel.getPassword());
              u.setRoles(createUserModel.getRoles());
              return ResponseEntity.ok(userModelAssembler.toModel(userService.save(u)));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  //...
}
```
_UserRestController.java_

To correctly handle password policy violations with correct http status we need to extend the _ErrorHandler_ class:

```java
package com.example.libraryserver.common.web;

import com.example.libraryserver.user.service.InvalidPasswordError;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice(annotations = RestController.class)
public class ErrorHandler {

  // ...

  @ExceptionHandler(InvalidPasswordError.class)
  public ResponseEntity<String> handle(InvalidPasswordError ex) {
    LOGGER.warn(ex.getMessage());
    return ResponseEntity.badRequest()
        .body(Encode.forJavaScriptSource(Encode.forHtmlContent(ex.getMessage())));
  }

  // ...
}
```

This is the end of step 2 of the workshop.

__NOTE:__ You find the completed code in project _lab2/library-server-complete_.

In the next lab we will add the MTLS authentication.





