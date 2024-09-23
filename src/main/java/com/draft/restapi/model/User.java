package com.draft.restapi.model;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.draft.restapi.audit.entity.AuditorBaseEntity;
import com.draft.restapi.audit.AuditListener;

@Getter
@Setter
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "users", uniqueConstraints = { // UNIQUE_X_KEY to able to extract field
		@UniqueConstraint(name = "\"uk_users_unique_username_key\"", columnNames = "username"),
		@UniqueConstraint(name = "\"uk_users_unique_email_key\"", columnNames = "email")
})
public class User extends AuditorBaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "email", nullable = false)
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
