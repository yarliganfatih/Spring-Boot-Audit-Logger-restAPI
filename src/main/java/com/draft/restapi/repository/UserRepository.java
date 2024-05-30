package com.draft.restapi.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.draft.restapi.model.User;

@RepositoryRestResource
public interface UserRepository extends PagingAndSortingRepository<User, Integer> {

}
