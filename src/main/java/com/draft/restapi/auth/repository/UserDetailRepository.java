package com.draft.restapi.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.draft.restapi.auth.entity.SignedUser;

public interface UserDetailRepository extends JpaRepository<SignedUser,Integer> {
    Optional<SignedUser> findByUsername(String name);
	boolean existsByUsername(String name);
}
