package com.draft.restapi.audit.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<ApiResponse<List<EntityLog>>> getEntities() {
		return ResponseEntity.ok(ApiResponse.success(logService.getEntities()));
	}

	@GetMapping("/entity/{entity_name}/{entity_id}")
	public ResponseEntity<ApiResponse<List<EntityLog>>> getEntitiyLogs(@PathVariable(name = "entity_name") String entityName, @PathVariable(name = "entity_id") Long entityId) {
		return ResponseEntity.ok(ApiResponse.success(logService.getEntitiyLogs(entityName, entityId)));
	}

	@GetMapping("/entity/{entity_name}/{entity_id}/updates/{path}")
	public ResponseEntity<ApiResponse<List<UpdateLog>>> getEntitiyPathUpdateLogs(@PathVariable(name = "entity_name") String entityName, @PathVariable(name = "entity_id") Long entityId, @PathVariable(name = "path") String path) {
		return ResponseEntity.ok(ApiResponse.success(logService.getEntitiyPathUpdateLogs(entityName, entityId, path)));
	}
}
