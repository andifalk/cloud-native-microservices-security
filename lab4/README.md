# Lab 4: Authorization

In this part of the workshop we want to add our customized authorization rules for our application.

As a result of the previous workshop steps we now have authentication for all our web endpoints
(including the actuator endpoints) and we can log in using our own users. But here security does not stop.

We know who is using our application (_authentication_) but we do not have control over what this user is
allowed to do in our application (_authorization_).

As a best practice the authorization should always be implemented on different layers like the web and method layer.
This way the authorization still prohibits access even if a user manages to bypass the web url based authorization filter
by playing around with manipulated URL's.

## Authorization Rules

Our required authorization rule matrix looks like this:

| URL | Http method                    | Restricted | Roles with access            |
| ---------| ------------------------ | -------- | --------------- |
| /*.css,/*.jpg,/*.ico,...   | All  | No   | --    |
| /books  | GET | Yes   | Authenticated    |
| /books  | POST,PUT,DELETE | Yes   | LIBRARY_CURATOR    |
| /books/{id}/borrow  | POST | Yes   | LIBRARY_USER    |
| /books/{id}/return  | POST | Yes   | LIBRARY_USER    |
| /users  | GET,POST,PUT,DELETE | Yes   | LIBRARY_ADMIN |
| /actuator/*    | GET   | Yes     | LIBRARY_ADMIN   |

## Web Layer Authorizations

All the web layer authorization rules are configured in the _WebSecurityConfiguration_ class by adding
more authorization rules:

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
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfiguration {

  //...

  @Configuration
  public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    public ApiWebSecurityConfigurationAdapter(
        @Qualifier("library-user-details-service") UserDetailsService userDetailsService) {
      this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      // http.csrf().disable();
      http.authorizeRequests(
              authorizeRequests ->
                  authorizeRequests
                      .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
                      .permitAll()
                      .requestMatchers(EndpointRequest.toAnyEndpoint())
                      .hasRole("LIBRARY_ACTUATOR")
                      .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                      .permitAll()
                      .mvcMatchers("/")
                      .permitAll()
                      .mvcMatchers(
                          POST,
                          "/books/{bookIdentifier}/borrow/{userIdentifier}",
                          "/books/{bookIdentifier}/return/{userIdentifier}")
                      .hasRole("LIBRARY_USER")
                      .mvcMatchers(POST, "/books")
                      .hasRole("LIBRARY_CURATOR")
                      .mvcMatchers(PUT, "/books/{bookIdentifier}")
                      .hasRole("LIBRARY_CURATOR")
                      .mvcMatchers(DELETE, "/books/{bookIdentifier}")
                      .hasRole("LIBRARY_CURATOR")
                      .mvcMatchers("/users", "/users/{userIdentifier}")
                      .hasRole("LIBRARY_ADMIN")
                      .anyRequest()
                      .authenticated())
          .httpBasic(withDefaults())
          .formLogin(withDefaults())
          .headers(h -> h.httpStrictTransportSecurity().disable())
          .x509(
              x -> {
                x.subjectPrincipalRegex("CN=(.*?),");
                x.userDetailsService(userDetailsService);
              });
    }
  }
}
```
_WebSecurityConfiguration.java_

By adding the annotation _@EnableGlobalMethodSecurity(prePostEnabled = true)_ the authorization on method layer is enabled.
The web layer authorization rules are added as combinations of _mvcMatchers()_ and _hasRole()_ statements.

## Method Layer Authorization

We continue with authorization on the method layer by adding the rules to our business service classes
_BookService_ and _UserService_. To achieve this we use the _@PreAuthorize_ annotations provided by spring security.
Same as other spring annotations (e.g. @Transactional) you can put _@PreAuthorize_ annotations on global class level
or on method level.

Depending on your authorization model you may use _@PreAuthorize_ to authorize using static roles or
to authorize using dynamic expressions (usually if you have roles with permissions).

![LogoutForm](images/roles_permissions.png)   
_Roles and Permissions_

If you want to have a permission based authorization you can use the predefined interface _PermissionEvaluator_ inside the
_@PreAuthorize_ annotations like this:

```java
class MyService {
    @PreAuthorize("hasPermission(#uuid, 'user', 'write')")
    void myOperation(UUID uuid) { /*...*/ }
}
```

```java
package org.springframework.security.access;

// ...

public interface PermissionEvaluator extends AopInfrastructureBean {

	boolean hasPermission(Authentication authentication, Object targetDomainObject,
			Object permission);

	boolean hasPermission(Authentication authentication, Serializable targetId,
			String targetType, Object permission);
}
```
_PermissionEvaluator_

In the workshop due to time constraints we have to keep things simple so we just use static roles.
Here it is done for the all operations of the book service.

```java
package com.example.libraryserver.book.service;

import com.example.libraryserver.book.data.Book;
import com.example.libraryserver.book.data.BookRepository;
import com.example.libraryserver.security.AuthenticatedUser;
import com.example.libraryserver.user.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@PreAuthorize("isAuthenticated()")
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

  public Optional<Book> findOneByIdentifier(UUID identifier) {
    LOGGER.trace("find book for identifier {}", identifier);
    return bookRepository.findOneByIdentifier(identifier);
  }

  public List<Book> findAll() {
    LOGGER.trace("find all books");
    return bookRepository.findAll();
  }

  @PreAuthorize("hasRole('LIBRARY_CURATOR')")
  @Transactional
  public Book save(Book book) {
    LOGGER.trace("Save book {}", book);

    if (book.getIdentifier() == null) {
      book.setIdentifier(idGenerator.generateId());
    }
    return bookRepository.save(book);
  }

  @PreAuthorize("hasRole('LIBRARY_USER')")
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

  @PreAuthorize("hasRole('LIBRARY_USER')")
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

  @PreAuthorize("hasRole('LIBRARY_CURATOR')")
  @Transactional
  public boolean deleteOneByIdentifier(UUID bookIdentifier) {
    LOGGER.trace("delete book with identifier {}", bookIdentifier);

    return bookRepository
        .findOneByIdentifier(bookIdentifier)
        .map(
            b -> {
              bookRepository.delete(b);
              return true;
            })
        .orElse(false);
  }
}
```
_BookService.java_

And now we add it the same way for the all operations of the user service.

```java
package com.example.libraryserver.user.service;

import com.example.libraryserver.user.data.User;
import com.example.libraryserver.user.data.UserRepository;
import org.owasp.security.logging.SecurityMarkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final IdGenerator idGenerator;

  public UserService(UserRepository userRepository, IdGenerator idGenerator) {
    this.userRepository = userRepository;
    this.idGenerator = idGenerator;
  }

  @PreAuthorize("hasRole('LIBRARY_ADMIN')")
  public Optional<User> findOneByIdentifier(UUID identifier) {
    return userRepository.findOneByIdentifier(identifier);
  }

  public Optional<User> findOneByEmail(String email) {
    return userRepository.findOneByEmail(email);
  }

  @PreAuthorize("hasRole('LIBRARY_ADMIN')")
  public List<User> findAll() {
    LOGGER.trace("find all users");

    return userRepository.findAll();
  }

  @PreAuthorize("hasRole('LIBRARY_ADMIN')")
  @Transactional
  public User save(User user) {
    LOGGER.info(SecurityMarkers.CONFIDENTIAL, "save user with password={}", user.getPassword());

    LOGGER.trace("save user {}", user);

    if (user.getIdentifier() == null) {
      user.setIdentifier(idGenerator.generateId());
    }
    return userRepository.save(user);
  }

  @PreAuthorize("hasRole('LIBRARY_ADMIN')")
  @Transactional
  public boolean deleteOneIdentifier(UUID userIdentifier) {
    LOGGER.trace("delete user with identifier {}", userIdentifier);

    return userRepository
        .findOneByIdentifier(userIdentifier)
        .map(
            u -> {
              userRepository.delete(u);
              return true;
            })
        .orElse(false);
  }
}
```
_UserService.java_

In this workshop lab we added the authorization to web and method layers. So now for particular RESTful endpoints access is only
permitted to users with special roles.

__NOTE:__ You find the completed code in project _lab4/library-server-complete_.

But how do you know that you have implemented all the authorization rules and did not leave a big security leak
for your RESTful API? Or you may change some authorizations later by accident?

To be on a safer side here you need automatic testing. Yes, this can also be done for security!
We will see how this works in the next workshop lab.





