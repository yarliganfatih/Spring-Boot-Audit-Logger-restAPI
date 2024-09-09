package com.draft.restapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Email;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    private String username;

    @Email
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
