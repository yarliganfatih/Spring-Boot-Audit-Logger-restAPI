package com.draft.restapi.audit.service;

import java.util.List;

import com.draft.restapi.audit.document.AuditLogDocument;

public interface LogService {
    Iterable<AuditLogDocument> getAuditLogs();

    List<AuditLogDocument> getEntityLogs(String entityName, Integer entityId);
}
