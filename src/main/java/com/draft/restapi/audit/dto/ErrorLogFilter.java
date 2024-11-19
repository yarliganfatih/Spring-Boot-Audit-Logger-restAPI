package com.draft.restapi.audit.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ErrorLogFilter implements Serializable {
    private String id;
    private String endpointUrl;
    private String httpMethod;
    private String errorType;
    private Integer httpStatusCode;
    private String traceId;
    private Integer occurredById;
    private String occurredByUsername;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
