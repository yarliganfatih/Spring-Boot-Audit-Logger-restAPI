package com.draft.restapi.auth.entity.oauth;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "oauth_code")
public class Code {

    @Id
    @Column(name = "code", nullable = false, updatable = false, length = 256)
    private String code;

    @Column(name = "authentication", columnDefinition = "longblob")
    private String authentication;

}
