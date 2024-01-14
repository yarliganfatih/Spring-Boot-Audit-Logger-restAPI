package com.draft.restapi.auth.entity.oauth;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "oauth_refresh_token")
public class RefreshToken {

    @Id
    @Column(name = "token_id", nullable = false, updatable = false, length = 256)
    private String tokenId;

    @Column(name = "token", columnDefinition = "longblob")
    private String token;

    @Column(name = "authentication", columnDefinition = "longblob")
    private String authentication;

}
