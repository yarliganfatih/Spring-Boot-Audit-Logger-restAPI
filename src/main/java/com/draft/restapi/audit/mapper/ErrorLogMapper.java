package com.draft.restapi.audit.mapper;

import com.draft.restapi.audit.document.ErrorLogDocument;
import com.draft.restapi.audit.dto.ErrorLogDto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ErrorLogMapper {

    ErrorLogDto toDto(ErrorLogDocument document);

    List<ErrorLogDto> toDtoList(List<ErrorLogDocument> documents);
}
