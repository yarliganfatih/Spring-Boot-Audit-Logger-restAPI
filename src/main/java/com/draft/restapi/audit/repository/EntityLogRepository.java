package com.draft.restapi.audit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.draft.restapi.audit.entity.EntityLog;

@Repository
public interface EntityLogRepository extends JpaRepository<EntityLog, Integer> {

    @Query(value = "SELECT * FROM audit_entity_logs WHERE entity_name = :entity_name AND entity_id = :entity_id", nativeQuery = true)
    public List<EntityLog> getEntitiyLogs(@Param("entity_name") String entity_name, @Param("entity_id") Long entity_id);

}