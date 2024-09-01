package com.draft.restapi.audit.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.audit.entity.UpdateLog;
import com.draft.restapi.audit.repository.EntityLogRepository;
import com.draft.restapi.audit.repository.UpdateLogRepository;
import com.draft.restapi.audit.service.LogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final EntityLogRepository entityLogRepository;

    private final UpdateLogRepository updateLogRepository;

    @Override
    public List<EntityLog> getEntities() {
        return entityLogRepository.findAll();
    }

    @Override
    public List<EntityLog> getEntitiyLogs(String entityName, Long entityId) {
        return entityLogRepository.getEntitiyLogs(entityName, entityId);
    }

    @Override
    public List<UpdateLog> getEntitiyPathUpdateLogs(String entityName, Long entityId, String path) {
        return updateLogRepository.getEntitiyPathUpdateLogs(entityName, entityId, path);
    }
}
