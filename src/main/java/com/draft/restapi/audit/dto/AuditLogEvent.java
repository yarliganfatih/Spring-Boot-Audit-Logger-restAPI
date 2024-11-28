package com.draft.restapi.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.draft.restapi.audit.document.FieldChange;

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
}
