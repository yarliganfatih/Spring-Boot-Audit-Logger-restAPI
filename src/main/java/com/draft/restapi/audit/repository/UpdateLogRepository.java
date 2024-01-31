package com.draft.restapi.audit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.draft.restapi.audit.entity.UpdateLog;

@Repository
public interface UpdateLogRepository extends JpaRepository<UpdateLog, Long> {

    @Query(value = "SELECT * FROM audit_update_logs aul JOIN audit_entity_logs ael ON ael.id = aul.log_id WHERE ael.entity_name = :entity_name AND ael.entity_id = :entity_id AND aul.path = :path", nativeQuery = true)
    public List<UpdateLog> getEntitiyPathUpdateLogs(@Param("entity_name") String entity_name, @Param("entity_id") Long entity_id, @Param("path") String path);

}
