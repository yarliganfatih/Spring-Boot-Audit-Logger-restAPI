package com.draft.restapi.model;

import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

import com.draft.restapi.audit.entity.AuditorBaseEntity;
import com.draft.restapi.audit.AuditListener;

@Getter
@Setter
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "users")
public class User extends AuditorBaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "username", nullable = false, unique = true)
	private String username;

    @Email
	@Column(name = "email", nullable = false, unique = true)
	private String email;

    @Column(name = "password", nullable = false)
    private String password;

	public User() {
	}

	public User(User user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
        this.password = user.getPassword();
	}
}
