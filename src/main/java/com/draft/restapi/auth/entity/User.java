package com.draft.restapi.auth.entity;

import org.springframework.security.core.context.SecurityContextHolder;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import com.draft.restapi.audit.AuditListener;
import com.draft.restapi.audit.entity.AuditorBaseEntity;
import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.auth.AuthUserDetail;

@Getter
@Setter
@Entity
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

    @Column(name = "enabled", nullable = false, columnDefinition = "boolean default true")
    private boolean enabled = true;
    
    @Column(name = "accountNonExpired", nullable = false, columnDefinition = "boolean default true")
    private boolean accountNonExpired = true;
    
    @Column(name = "credentialsNonExpired", nullable = false, columnDefinition = "boolean default true")
    private boolean credentialsNonExpired = true;
    
    @Column(name = "accountNonLocked", nullable = false, columnDefinition = "boolean default true")
    private boolean accountNonLocked = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", 
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")}, 
        inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")})
    private List<Role> roles;

    @OneToMany(mappedBy = "operated_by", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntityLog> entityLogs;
	
    public User() {
    }

    public User(User user) {
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
    
    public static User getLoggedUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof AuthUserDetail) {
                return ((User) principal);  
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                User mockUser = new User();
                mockUser.setId(1);
                return mockUser;  
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
