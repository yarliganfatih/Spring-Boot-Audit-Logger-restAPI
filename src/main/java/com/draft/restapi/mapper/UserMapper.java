package com.draft.restapi.mapper;

import com.draft.restapi.model.User;
import com.draft.restapi.model.dto.UserDto;
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
