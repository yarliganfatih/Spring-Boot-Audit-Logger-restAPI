package com.draft.restapi.auth.entity.oauth;

import javax.persistence.*;
import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "oauth_approvals")
public class Approvals {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false, length = 256)
    private String userId;

    @Column(name = "client_id", length = 256)
    private String clientId;

    @Column(name = "scope", length = 256)
    private String scope;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

}
