package com.draft.restapi.audit;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.audit.entity.UpdateLog;
import com.draft.restapi.audit.repository.EntityLogRepository;
import com.draft.restapi.audit.repository.UpdateLogRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    private final EntityLogRepository entityLogRepository;

	private final UpdateLogRepository updateLogRepository;

	@GetMapping("/entity/")
	public List<EntityLog> getEntities() {
		return entityLogRepository.findAll();
	}
    
	@GetMapping("/entity/{entity_name}/{entity_id}")
	public List<EntityLog> getEntitiyLogs(@PathVariable(name="entity_name") String entity_name, @PathVariable(name="entity_id") Long entity_id) {
		return entityLogRepository.getEntitiyLogs(entity_name, entity_id);
	}
    
	@GetMapping("/entity/{entity_name}/{entity_id}/updates/{path}")
	public List<UpdateLog> getEntitiyPathUpdateLogs(@PathVariable(name="entity_name") String entity_name, @PathVariable(name="entity_id") Long entity_id, @PathVariable(name="path") String path) {
		return updateLogRepository.getEntitiyPathUpdateLogs(entity_name, entity_id, path);
	}
}
