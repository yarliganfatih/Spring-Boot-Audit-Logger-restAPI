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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "audit-logs")
public class AuditLogDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String entityName;

    @Field(type = FieldType.Integer)
    private Integer entityId;

    @Field(type = FieldType.Keyword)
    private String operation;

    @Field(type = FieldType.Integer)
    private Integer operatorId;

    @Field(type = FieldType.Keyword)
    private String operatorName;

    @Field(type = FieldType.Nested)
    private List<FieldChange> changes;

    @Field(type = FieldType.Keyword)
    private String traceId;

    @Field(type = FieldType.Date)
    private LocalDateTime timestamp;
}
