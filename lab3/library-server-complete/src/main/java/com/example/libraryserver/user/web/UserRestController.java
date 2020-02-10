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

  @ResponseStatus(OK)
  @GetMapping
  public CollectionModel<UserModel> listAllUsers() {
    CollectionModel<UserModel> userListModel =
        userModelAssembler.toCollectionModel(userService.findAll());
    userListModel.add(linkTo(UserRestController.class).withSelfRel());
    return userListModel;
  }

  @GetMapping("/{userIdentifier}")
  public ResponseEntity<UserModel> getSingleUser(
      @PathVariable("userIdentifier") UUID userIdentifier) {
    return userService
        .findOneByIdentifier(userIdentifier)
        .map(u -> ResponseEntity.ok(userModelAssembler.toModel(u)))
        .orElse(ResponseEntity.notFound().build());
  }

  @ResponseStatus(NO_CONTENT)
  @DeleteMapping("/{userIdentifier}")
  public ResponseEntity<Void> deleteUser(@PathVariable("userIdentifier") UUID userIdentifier) {
    return userService.deleteOneIdentifier(userIdentifier)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
