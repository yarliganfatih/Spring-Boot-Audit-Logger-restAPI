package com.draft.restapi.audit.mapper;

import com.draft.restapi.audit.document.AuditLogDocument;
import com.draft.restapi.audit.dto.AuditLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditLogMapper {
    AuditLogDto toDto(AuditLogDocument document);
    List<AuditLogDto> toDtoList(List<AuditLogDocument> documents);
}
