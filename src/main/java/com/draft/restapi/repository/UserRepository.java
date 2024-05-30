package com.draft.restapi.repository;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.jpa.repository.Query;

import com.draft.restapi.model.User;

@RepositoryRestResource
public interface UserRepository extends PagingAndSortingRepository<User, Integer> {

    @Query(value = "SELECT * FROM users s WHERE s.username = :username", nativeQuery = true)
    Page<User> findByUsername (
        @Param(value = "username") String username,
        Pageable pageable
    );

    @RestResource(exported = false)
    Optional<User> findByUsername(String username);
}
