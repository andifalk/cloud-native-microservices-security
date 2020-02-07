package com.example.libraryserver.user.web;

import com.example.libraryserver.user.data.User;
import org.owasp.encoder.Encode;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler extends RepresentationModelAssemblerSupport<User, UserModel> {

  public UserModelAssembler() {
    super(UserRestController.class, UserModel.class);
  }

  @Override
  public UserModel toModel(User user) {
    UserModel userModel =
        outputEscaping(
            new UserModel(
                user.getIdentifier(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles()));
    userModel.add(
        linkTo(methodOn(UserRestController.class).getSingleUser(userModel.getIdentifier()))
            .withSelfRel());

    return userModel;
  }

  @Override
  public CollectionModel<UserModel> toCollectionModel(Iterable<? extends User> entities) {

    List<UserModel> result = new ArrayList<>();

    for (User entity : entities) {
      result.add(toModel(entity));
    }

    return new UserModelList(result);
  }

  private UserModel outputEscaping(UserModel input) {
    UserModel output = new UserModel();
    output.setEmail(Encode.forJavaScript(Encode.forHtml(input.getEmail())));
    output.setFirstName(Encode.forJavaScript(Encode.forHtml(input.getFirstName())));
    output.setLastName(Encode.forJavaScript(Encode.forHtml(input.getLastName())));
    output.setIdentifier(input.getIdentifier());
    for (String role : input.getRoles()) {
      output.getRoles().add(Encode.forJavaScript(Encode.forHtml(role)));
    }

    return output;
  }
}
