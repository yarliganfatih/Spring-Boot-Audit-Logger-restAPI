package com.draft.restapi.audit.service;

import java.util.List;

import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.audit.entity.UpdateLog;

public interface LogService {
    List<EntityLog> getEntities();

    List<EntityLog> getEntityLogs(String entityName, Long entityId);

    List<UpdateLog> getEntityPathUpdateLogs(String entityName, Long entityId, String path);
}
