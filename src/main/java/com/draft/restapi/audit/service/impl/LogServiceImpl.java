package com.draft.restapi.audit.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.draft.restapi.audit.document.AuditLogDocument;
import com.draft.restapi.audit.repository.AuditLogElasticRepository;
import com.draft.restapi.audit.service.LogService;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private AuditLogElasticRepository auditLogRepository;

    @Override
    public Iterable<AuditLogDocument> getAuditLogs() {
        return auditLogRepository.findAll();
    }

    @Override
    public List<AuditLogDocument> getEntityLogs(String entityName, Integer entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
    }
}
