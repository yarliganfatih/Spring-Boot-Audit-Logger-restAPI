package com.draft.restapi.audit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;

import com.flipkart.zjsonpatch.JsonDiff;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.draft.restapi.audit.entity.AuditorBaseEntity;
import com.draft.restapi.audit.document.FieldChange;
import com.draft.restapi.audit.dto.AuditLogEvent;
import com.draft.restapi.auth.entity.User;
import com.draft.restapi.common.filter.TraceFilter;
import com.draft.restapi.common.masking.MaskType;
import com.draft.restapi.common.masking.MaskUtils;

@Slf4j
public class AuditListener {
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT_LOGGER");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @PostLoad
    public void postRead(final AuditorBaseEntity entity){
        entity.setJsonObject(entity.transformJsonObject()); // for PreUpdate
        // logEvent(entity, "READ", null); // for record READ operations
    }

    public JsonNode getChanges(JsonNode beforeNode, JsonNode afterNode){
        return JsonDiff.asJson(afterNode, beforeNode);
    }

    @PreUpdate
    public void preUpdate(AuditorBaseEntity entity) {
        JsonNode changes = getChanges(entity.getJsonObject(), entity.transformJsonObject());
        logEvent(entity, "UPDATE", changes);
    }

    @PrePersist
    public void preCreate(AuditorBaseEntity entity) {
        // Handled in PostPersist to have the generated ID
    }

    @PreRemove
    public void preDelete(AuditorBaseEntity entity) {
        logEvent(entity, "DELETE", null);
    }

    @PostPersist
    public void postCreate(AuditorBaseEntity entity) {
        logEvent(entity, "CREATE", null);
    }

    private void logEvent(AuditorBaseEntity entity, String operation, JsonNode changes) {
        AuditLogEvent auditLog = new AuditLogEvent();
        try {
            auditLog.setOperation(operation);
            auditLog.setTraceId(MDC.get(TraceFilter.TRACE_ID));
            auditLog.setEntityName(entity.getTableName());
            auditLog.setEntityId(entity.getId());
            auditLog.setTimestamp(LocalDateTime.now());

            User loggedUser = User.getLoggedUser();
            if (loggedUser != null) {
                auditLog.setOperatorId(loggedUser.getId());
                auditLog.setOperatorName(loggedUser.getUsername());
            }

            List<FieldChange> changesList = new ArrayList<>();
            if (changes != null && changes.isArray()) {
                for (JsonNode change : changes) {
                    if (change.isNull()) continue;
                    String changedPath = change.has("path") ? change.get("path").asText() : "ALL";
                    String fieldName = changedPath.substring(changedPath.lastIndexOf("/") + 1);
                    String previousValue = change.has("value") ? change.get("value").asText() : null;
                    String maskedPreviousValue = MaskType.by(fieldName).mask(previousValue);
                    changesList.add(new FieldChange(fieldName, maskedPreviousValue));
                }
            }
            auditLog.setChanges(changesList);

            AUDIT_LOGGER.info(MaskUtils.maskJsonFields(OBJECT_MAPPER.writeValueAsString(auditLog)));
        } catch (Exception e) { // do not affect main flow if auditLog saving fails
            log.warn("Failed to write auditLog to file: {}", e.getMessage(), e);
            log.debug("Missing auditLog details: {}", auditLog);
        }
    }
}
