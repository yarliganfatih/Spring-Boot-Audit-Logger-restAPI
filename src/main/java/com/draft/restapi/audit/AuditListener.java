package com.draft.restapi.audit;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.zjsonpatch.JsonDiff;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.draft.restapi.audit.entity.AuditorBaseEntity;
import com.draft.restapi.audit.dto.AuditLogEvent;
import com.draft.restapi.auth.entity.User;

public class AuditListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditListener.class);
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
        try {
            User loggedUser = User.getLoggedUser();
            AuditLogEvent event = AuditLogEvent.create(entity, operation, loggedUser, changes);
            AUDIT_LOGGER.info(OBJECT_MAPPER.writeValueAsString(event));
        } catch (Exception e) {
            LoggerFactory.getLogger(AuditListener.class).error("Error writing audit log", e);
        }
    }
}
