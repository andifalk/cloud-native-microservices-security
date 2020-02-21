package com.example.libraryserver.user.web;

import org.springframework.hateoas.CollectionModel;

import java.util.Collection;

public class UserModelList extends CollectionModel<UserModel> {

  private final Collection<UserModel> users;

  public UserModelList(Collection<UserModel> users) {
    this.users = users;
  }

  public Collection<UserModel> getUsers() {
    return users;
  }
}
