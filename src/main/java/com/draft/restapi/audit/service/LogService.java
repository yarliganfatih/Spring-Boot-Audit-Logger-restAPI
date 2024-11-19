package com.draft.restapi.audit.service;

import com.draft.restapi.common.payload.PageDto;

import org.springframework.data.domain.Pageable;

import com.draft.restapi.audit.dto.AuditLogDto;
import com.draft.restapi.audit.dto.AuditLogFilter;
import com.draft.restapi.audit.dto.UpdateHistoryDto;

import com.draft.restapi.audit.dto.ErrorLogDto;
import com.draft.restapi.audit.dto.ErrorLogFilter;

public interface LogService {
    PageDto<AuditLogDto> getAuditLogs(AuditLogFilter filter, Pageable pageable);

    PageDto<ErrorLogDto> getErrorLogs(ErrorLogFilter filter, Pageable pageable);

    PageDto<UpdateHistoryDto> getEntityFieldUpdateLogs(Pageable pageable, String entityName, Integer entityId, String fieldName);
}
