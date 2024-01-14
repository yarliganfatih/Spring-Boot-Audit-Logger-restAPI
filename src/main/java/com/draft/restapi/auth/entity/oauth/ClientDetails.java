package com.draft.restapi.auth.entity.oauth;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "oauth_client_details")
public class ClientDetails {

    @Id
    @Column(name = "client_id", nullable = false, updatable = false)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "web_server_redirect_uri", length = 2048)
    private String webServerRedirectUri;

    @Column(name = "scope")
    private String scope;

    @Column(name = "access_token_validity")
    private Integer accessTokenValidity;

    @Column(name = "refresh_token_validity")
    private Integer refreshTokenValidity;

    @Column(name = "resource_ids", length = 1024)
    private String resourceIds;

    @Column(name = "authorized_grant_types", length = 1024)
    private String authorizedGrantTypes;

    @Column(name = "authorities", length = 1024)
    private String authorities;

    @Column(name = "additional_information", length = 4096)
    private String additionalInformation;

    @Column(name = "autoapprove")
    private String autoapprove;

}
