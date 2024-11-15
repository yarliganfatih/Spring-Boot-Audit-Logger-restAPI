package com.draft.restapi.audit.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private String id;
    private String entityName;
    private Integer entityId;
    private String operation;
    private Integer operatorId;
    private String operatorName;
    private List<FieldChangeDto> changes;
    private String traceId;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldChangeDto {
        private String fieldName;
        private String previousValue;
    }
}
