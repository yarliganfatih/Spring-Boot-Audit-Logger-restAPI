package com.draft.restapi.audit.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

@Data
public class AuditLogFilter implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String entityName;

    private Integer entityId;

    private String operation;

    private Integer operatorId;

    private String operatorName;

    private String traceId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
}
