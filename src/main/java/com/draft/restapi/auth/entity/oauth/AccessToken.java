package com.draft.restapi.auth.entity.oauth;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "oauth_access_token")
public class AccessToken {

    @Id
    @Column(name = "token_id", nullable = false, updatable = false, length = 256)
    private String tokenId;

    @Column(name = "token", columnDefinition = "longblob")
    private String token;

    @Column(name = "authentication_id", nullable = false, length = 256)
    private String authenticationId;

    @Column(name = "user_name", length = 256)
    private String userName;

    @Column(name = "client_id", length = 256)
    private String clientId;

    @Column(name = "authentication", columnDefinition = "longblob")
    private String authentication;

    @Column(name = "refresh_token", length = 256)
    private String refreshToken;

}
