package com.draft.restapi.auth.entity.dto;

import com.draft.restapi.common.validation.ValidationGroups;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    @NotBlank(groups = {ValidationGroups.OnCreate.class})
    @Size(min = 3, max = 64)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @Email
    @NotBlank(groups = {ValidationGroups.OnCreate.class})
    @Size(min = 3, max = 320)
    private String email;

    @NotBlank(groups = {ValidationGroups.OnCreate.class})
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Password must be at least 8 characters long and contain both letters and numbers")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
