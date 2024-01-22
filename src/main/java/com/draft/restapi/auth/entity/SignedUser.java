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

    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "password")
    private String password;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "enabled", nullable = false, columnDefinition = "boolean default true")
    private boolean enabled = true;
    
    @Column(name = "accountNonExpired", nullable = false, columnDefinition = "boolean default true")
    private boolean accountNonExpired = true;
    
    @Column(name = "credentialsNonExpired", nullable = false, columnDefinition = "boolean default true")
    private boolean credentialsNonExpired = true;
    
    @Column(name = "accountNonLocked", nullable = false, columnDefinition = "boolean default true")
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
