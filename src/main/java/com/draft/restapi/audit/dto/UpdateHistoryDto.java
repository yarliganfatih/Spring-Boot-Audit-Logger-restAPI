package com.draft.restapi.audit.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHistoryDto {
    private String auditLogId;
    private String traceId;
    private LocalDateTime timestamp;
    private Integer operatorId;
    private String operatorName;
    private String previousValue;
}
