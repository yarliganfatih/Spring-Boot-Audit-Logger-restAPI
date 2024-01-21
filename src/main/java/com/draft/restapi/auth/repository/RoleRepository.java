package com.draft.restapi.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.draft.restapi.auth.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(value = "SELECT * FROM roles WHERE level IS NOT NULL ORDER BY level ASC", nativeQuery = true)
    public List<Role> getRoleHierarchyQuery();

}
