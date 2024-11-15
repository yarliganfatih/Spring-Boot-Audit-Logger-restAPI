package com.draft.restapi.audit.service;

import com.draft.restapi.common.payload.PageDto;
import org.springframework.data.domain.Pageable;

import com.draft.restapi.audit.dto.AuditLogDto;
import com.draft.restapi.audit.dto.AuditLogFilter;

public interface LogService {
    PageDto<AuditLogDto> getAuditLogs(AuditLogFilter filter, Pageable pageable);
}
