package com.draft.restapi.auth.entity;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.springframework.security.core.context.SecurityContextHolder;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.auth.AuthUserDetail;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Getter
@Setter
@Entity
@Table(name = "users")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY) // to avoid infinite loop
public class SignedUser implements Serializable {
   
	private static final long serialVersionUID = 244852266L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "enabled", nullable = false, columnDefinition = "boolean default true")
    private boolean enabled = true;
    
    @JsonIgnore
    @Column(name = "accountNonExpired", nullable = false, columnDefinition = "boolean default true")
    private boolean accountNonExpired = true;
    
    @JsonIgnore
    @Column(name = "credentialsNonExpired", nullable = false, columnDefinition = "boolean default true")
    private boolean credentialsNonExpired = true;
    
    @JsonIgnore
    @Column(name = "accountNonLocked", nullable = false, columnDefinition = "boolean default true")
    private boolean accountNonLocked = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {
                    @JoinColumn(name = "role_id", referencedColumnName = "id")})
    private List<Role> roles;

    @OneToMany(mappedBy = "operated_by", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntityLog> entityLogs;
	
    public SignedUser() {
    }

    public SignedUser(SignedUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();
        this.accountNonExpired = user.isAccountNonExpired();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
		this.roles = user.getRoles();
	}
    
    public static SignedUser getLoggedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserDetail) {
            return ((SignedUser) principal);  
        }
        return null;
    }
}
