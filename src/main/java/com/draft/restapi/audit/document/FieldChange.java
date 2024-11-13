package com.draft.restapi.audit.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldChange {

    @Field(type = FieldType.Keyword)
    private String fieldName;

    @Field(type = FieldType.Text)
    private String previousValue;
}
