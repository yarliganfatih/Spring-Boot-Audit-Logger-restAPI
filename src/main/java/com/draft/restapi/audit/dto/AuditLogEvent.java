package com.draft.restapi.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.MDC;

import com.draft.restapi.audit.document.FieldChange;
import com.draft.restapi.audit.entity.AuditorBaseEntity;
import com.draft.restapi.auth.entity.User;
import com.draft.restapi.common.filter.TraceFilter;
import com.fasterxml.jackson.databind.JsonNode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEvent {
    private String id;
    private String entityName;
    private Integer entityId;
    private String operation;
    private Integer operatorId;
    private String operatorName;
    private List<FieldChange> changes;
    private String traceId;
    private LocalDateTime timestamp;

    public static AuditLogEvent create(AuditorBaseEntity entity, String operation, User operator, JsonNode changesNode) {
        String traceId = MDC.get(TraceFilter.TRACE_ID);
        List<FieldChange> changesList = new java.util.ArrayList<>();

        if (changesNode != null && changesNode.isArray()) {
            for (JsonNode change : changesNode) {
                if (change.isNull()) continue;
                String changedPath = change.has("path") ? change.get("path").asText() : "ALL";
                String fieldName = changedPath.substring(changedPath.lastIndexOf("/") + 1);
                String previousValue = change.has("value") ? change.get("value").asText() : null;
                changesList.add(new FieldChange(fieldName, previousValue));
            }
        }

        return AuditLogEvent.builder()
                .id(UUID.randomUUID().toString())
                .entityName(entity.getTableName())
                .entityId(entity.getId())
                .operation(operation)
                .operatorId(operator.getId())
                .operatorName(operator.getUsername())
                .changes(changesList)
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
