package com.draft.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.draft.restapi.model.User;

public interface UserRepository extends JpaRepository<User, Integer>{

}
