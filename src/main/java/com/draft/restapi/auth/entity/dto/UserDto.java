package com.draft.restapi.auth.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @Email
    @NotBlank
    @Size(max = 320)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Password must be at least 8 characters long and contain both letters and numbers")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    @Getter
    @Setter
    public static class Filter {
        private Integer id;
        private String username;
        private String email;
    }
}
