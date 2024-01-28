package com.draft.restapi.audit;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import com.flipkart.zjsonpatch.JsonDiff;
import lombok.RequiredArgsConstructor;

import com.draft.restapi.audit.entity.AuditorBaseEntity;
import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.audit.entity.UpdateLog;
import com.draft.restapi.audit.repository.EntityLogRepository;
import com.draft.restapi.audit.repository.UpdateLogRepository;
import com.fasterxml.jackson.databind.JsonNode;

@RequiredArgsConstructor
public class AuditListener {
    
	private final EntityLogRepository entityLogRepository;

	private final UpdateLogRepository updateLogRepository;

    public String getLoggedUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();  
        } else {
            return principal.toString();
        }
    }

    @PostLoad
    public void postRead(final AuditorBaseEntity entity){
        entity.setJsonObject(entity.transformJsonObject()); // for PreUpdate
        // entity = preOperation(entity, "READ"); // for record READ operations
    }
        
    public JsonNode getChanges(JsonNode beforeNode, JsonNode afterNode){
        return JsonDiff.asJson(afterNode, beforeNode);
    }

    @PreUpdate
    public void preUpdate(AuditorBaseEntity entity) {
        entity = preOperation(entity, "UPDATE");
        EntityLog entityLog = entity.getEntityLog();
        JsonNode changes = getChanges(entity.getJsonObject(), entity.transformJsonObject());
        
        changes.forEach(change -> {
            UpdateLog updateLog = new UpdateLog();
            updateLog.setEntityLog(entityLog);
            updateLog.setOp(change.get("op").asText());
            String changedPath = change.get("path").asText();
            updateLog.setPath(changedPath.substring(changedPath.lastIndexOf("/") + 1));
            updateLog.setPreviousValue(change.get("value").asText());
            updateLogRepository.save(updateLog);
        });
    }
    
    @PrePersist
    public void preCreate(AuditorBaseEntity entity) {
        entity = preOperation(entity, "CREATE");
    }
    
    @PreRemove
    public void preDelete(AuditorBaseEntity entity) {
        entity = preOperation(entity, "DELETE");
    }

    public AuditorBaseEntity preOperation(AuditorBaseEntity entity, String operation) {
        EntityLog entityLog = new EntityLog(entity.getTableName(), entity.getId(), operation, getLoggedUserName());
        entityLog = entityLogRepository.save(entityLog);
        entity.setEntityLog(entityLog); // for usable on postOperation
        return entity;
    }

    @PostUpdate
    @PostRemove
    @PostPersist
    @Transactional
    public void postOperation(AuditorBaseEntity entity) {
        EntityLog entityLog = entity.getEntityLog();
        entityLog.setCompleted(true); // TODO does not work on PostUpdate and PostRemove
        entityLog.setEntity_id(entity.getId()); // for PostPersist
        entityLog = entityLogRepository.save(entityLog);
    }
}
