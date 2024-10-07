package com.draft.restapi.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.draft.restapi.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String name);

	boolean existsByUsername(String name);
}
