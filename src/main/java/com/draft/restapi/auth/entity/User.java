package com.draft.restapi.auth.entity;

import org.springframework.security.core.context.SecurityContextHolder;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import com.draft.restapi.audit.AuditListener;
import com.draft.restapi.audit.entity.AuditorBaseEntity;
import com.draft.restapi.auth.AuthUserDetail;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email") })
@EntityListeners(AuditListener.class)
public class User extends AuditorBaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "enabled", columnDefinition = "boolean default true")
    private Boolean enabled;
    
    @Column(name = "accountNonExpired", columnDefinition = "boolean default true")
    private Boolean accountNonExpired;
    
    @Column(name = "credentialsNonExpired", columnDefinition = "boolean default true")
    private Boolean credentialsNonExpired;
    
    @Column(name = "accountNonLocked", columnDefinition = "boolean default true")
    private Boolean accountNonLocked;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    private Boolean deleted;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", 
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")}, 
        inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    private List<Role> roles;
	
    public User() {
    }

    public User(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.enabled = user.getEnabled();
        this.accountNonExpired = user.getAccountNonExpired();
        this.credentialsNonExpired = user.getCredentialsNonExpired();
        this.accountNonLocked = user.getAccountNonLocked();
        this.deleted = user.getDeleted();
		this.roles = user.getRoles();
	}
    
    public static User getLoggedUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof AuthUserDetail) {
                return ((User) principal);  
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                User mockUser = new User();
                mockUser.setId(1);
                mockUser.setUsername(((org.springframework.security.core.userdetails.User) principal).getUsername());
                return mockUser;  
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
