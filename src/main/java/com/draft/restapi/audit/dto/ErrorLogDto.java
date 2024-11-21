package com.draft.restapi.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private List<Map<String, String>> methodArguments;
    private String errorMessage;
    private String errorStackTrace;
    private String errorType;
    private Integer httpStatusCode;
    private String traceId;
    private Integer occurredById;
    private String occurredByUsername;
    private LocalDateTime timestamp;
}
