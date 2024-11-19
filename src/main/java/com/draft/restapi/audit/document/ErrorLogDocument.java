package com.draft.restapi.audit.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "error-logs")
public class ErrorLogDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String endpointUrl;

    @Field(type = FieldType.Keyword)
    private String httpMethod;

    @Field(type = FieldType.Text)
    private String requestHeaders;

    @Field(type = FieldType.Text)
    private String requestParams;

    @Field(type = FieldType.Text)
    private String requestBody;

    @Field(type = FieldType.Text)
    private String responseBody;

    @Field(type = FieldType.Text)
    private String errorMessage;

    @Field(type = FieldType.Text)
    private String errorStackTrace;

    @Field(type = FieldType.Keyword)
    private String errorType;

    @Field(type = FieldType.Integer)
    private Integer httpStatusCode;

    @Field(type = FieldType.Keyword)
    private String traceId;

    @Field(type = FieldType.Integer)
    private Integer occurredById;

    @Field(type = FieldType.Keyword)
    private String occurredByUsername;

    @Field(type = FieldType.Date)
    private LocalDateTime timestamp;
}
