package com.example.libraryserver.user.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findOneByIdentifier(UUID identifier);

  Optional<User> findOneByEmail(String email);
}
