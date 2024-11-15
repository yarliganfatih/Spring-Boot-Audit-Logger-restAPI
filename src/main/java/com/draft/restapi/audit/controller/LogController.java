package com.draft.restapi.audit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.draft.restapi.audit.dto.AuditLogDto;
import com.draft.restapi.audit.dto.AuditLogFilter;
import com.draft.restapi.audit.service.LogService;
import com.draft.restapi.common.payload.ApiResponse;

@RestController
@RequestMapping("/api/logs")
public class LogController {

	@Autowired
    private LogService logService;

    @GetMapping("/audit")
    public ApiResponse<AuditLogDto> getAuditLogs(
            AuditLogFilter filter,
            @PageableDefault(page = 0, size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(logService.getAuditLogs(filter, pageable));
    }
}
