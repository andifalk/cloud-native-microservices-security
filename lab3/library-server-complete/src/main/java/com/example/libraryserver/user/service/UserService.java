package com.example.libraryserver.user.service;

import com.example.libraryserver.user.data.User;
import com.example.libraryserver.user.data.UserRepository;
import org.owasp.security.logging.SecurityMarkers;
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
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final IdGenerator idGenerator;

  public UserService(UserRepository userRepository, IdGenerator idGenerator) {
    this.userRepository = userRepository;
    this.idGenerator = idGenerator;
  }

  public Optional<User> findOneByIdentifier(UUID identifier) {
    return userRepository.findOneByIdentifier(identifier);
  }

  public Optional<User> findOneByEmail(String email) {
    return userRepository.findOneByEmail(email);
  }

  public List<User> findAll() {
    LOGGER.trace("find all users");

    return userRepository.findAll();
  }

  @Transactional
  public User save(User user) {
    LOGGER.info(SecurityMarkers.CONFIDENTIAL, "save user with password={}", user.getPassword());

    LOGGER.trace("save user {}", user);

    if (user.getIdentifier() == null) {
      user.setIdentifier(idGenerator.generateId());
    }
    return userRepository.save(user);
  }

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
