package com.draft.restapi.auth.entity;

import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class SignedUser implements Serializable {
   
	private static final long serialVersionUID = 244852266L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotNull
    @Column(name = "username", unique = true)
    private String username;
    
    @NotNull
    @Column(name = "password")
    private String password;

    @Email
    @NotNull
    @Column(name = "email", unique = true)
    private String email;

    @NotNull
    @Column(name = "enabled", columnDefinition = "boolean default true")
    private boolean enabled = true;
    
    @NotNull
    @Column(name = "accountNonExpired", columnDefinition = "boolean default true")
    private boolean accountNonExpired = true;
    
    @NotNull
    @Column(name = "credentialsNonExpired", columnDefinition = "boolean default true")
    private boolean credentialsNonExpired = true;
    
    @NotNull
    @Column(name = "accountNonLocked", columnDefinition = "boolean default true")
    private boolean accountNonLocked = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {
                    @JoinColumn(name = "role_id", referencedColumnName = "id")})
    private List<Role> roles;
	
    public SignedUser() {
    }

    public SignedUser(SignedUser user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();
        this.accountNonExpired = user.isAccountNonExpired();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
		this.roles = user.getRoles();
	}
}
