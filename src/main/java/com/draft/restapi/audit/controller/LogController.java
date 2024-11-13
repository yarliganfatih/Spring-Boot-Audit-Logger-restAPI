package com.draft.restapi.audit.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.draft.restapi.audit.document.AuditLogDocument;
import com.draft.restapi.audit.service.LogService;

import com.draft.restapi.common.payload.ApiResponse;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/logs")
public class LogController {

	@Autowired
	private LogService logService;

	@GetMapping("/audit/")
	public ApiResponse<Iterable<AuditLogDocument>> getAuditLogs() {
		return ApiResponse.success(logService.getAuditLogs());
	}

	@GetMapping("/audit/{entity_name}/{entity_id}")
	public ApiResponse<List<AuditLogDocument>> getEntityLogs(
			@PathVariable(name = "entity_name") String entityName, 
			@PathVariable(name = "entity_id") Integer entityId) {
		return ApiResponse.success(logService.getEntityLogs(entityName, entityId));
	}
}
