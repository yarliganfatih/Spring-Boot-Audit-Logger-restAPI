package com.draft.restapi.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.draft.restapi.audit.entity.UpdateLog;

@Repository
public interface UpdateLogRepository extends JpaRepository<UpdateLog, Long> {

}
