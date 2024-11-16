package com.draft.restapi.audit.mapper;

import com.draft.restapi.audit.document.AuditLogDocument;
import com.draft.restapi.audit.dto.AuditLogDto;
import com.draft.restapi.audit.dto.UpdateHistoryDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditLogMapper {

    AuditLogDto toDto(AuditLogDocument document);

    List<AuditLogDto> toDtoList(List<AuditLogDocument> documents);

    @Mapping(target = "auditLogId", source = "document.id")
    @Mapping(target = "previousValue", expression = "java(previousValue)")
    UpdateHistoryDto toUpdateHistoryDto(AuditLogDocument document, String previousValue);
}
