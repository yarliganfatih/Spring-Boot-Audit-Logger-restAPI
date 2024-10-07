package com.draft.restapi.auth.mapper;

import com.draft.restapi.auth.entity.dto.UserDto;
import com.draft.restapi.auth.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    User toEntity(UserDto userDto);

    UserDto toDto(User entity);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateUserFromDto(UserDto userDto, @MappingTarget User user);
}
