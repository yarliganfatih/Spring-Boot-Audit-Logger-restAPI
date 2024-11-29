package com.draft.restapi.auth.entity.dto;

import java.io.Serializable;

import lombok.Data;

@Data 
public class UserFilter implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String username;
    private String email;
}
