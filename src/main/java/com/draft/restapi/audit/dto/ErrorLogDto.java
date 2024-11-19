package com.draft.restapi.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLogDto {
    private String id;
    private String endpointUrl;
    private String httpMethod;
    private String requestHeaders;
    private String requestParams;
    private String requestBody;
    private String responseBody;
    private String errorMessage;
    private String errorStackTrace;
    private String errorType;
    private Integer httpStatusCode;
    private String traceId;
    private Integer occurredById;
    private String occurredByUsername;
    private LocalDateTime timestamp;
}
