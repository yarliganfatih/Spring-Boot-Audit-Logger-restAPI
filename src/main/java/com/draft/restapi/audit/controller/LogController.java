package com.draft.restapi.audit.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.audit.entity.UpdateLog;
import com.draft.restapi.audit.service.LogService;

import com.draft.restapi.common.payload.ApiResponse;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/logs")
public class LogController {

	@Autowired
	private LogService logService;

	@GetMapping("/entity/")
	public ApiResponse<List<EntityLog>> getEntities() {
		return ApiResponse.success(logService.getEntities());
	}

	@GetMapping("/entity/{entity_name}/{entity_id}")
	public ApiResponse<List<EntityLog>> getEntitiyLogs(
			@PathVariable(name = "entity_name") String entityName, 
			@PathVariable(name = "entity_id") Long entityId) {
		return ApiResponse.success(logService.getEntityLogs(entityName, entityId));
	}

	@GetMapping("/entity/{entity_name}/{entity_id}/updates/{path}")
	public ApiResponse<List<UpdateLog>> getEntityPathUpdateLogs(
			@PathVariable(name = "entity_name") String entityName, 
			@PathVariable(name = "entity_id") Long entityId, 
			@PathVariable(name = "path") String path) {
		return ApiResponse.success(logService.getEntityPathUpdateLogs(entityName, entityId, path));
	}
}
