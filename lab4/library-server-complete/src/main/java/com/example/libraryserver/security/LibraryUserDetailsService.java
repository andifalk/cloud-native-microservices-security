package com.example.libraryserver.security;

import com.example.libraryserver.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
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

  @PreAuthorize("isAnonymous()")
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
