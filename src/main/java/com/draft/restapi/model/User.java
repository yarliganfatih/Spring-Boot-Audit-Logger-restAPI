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
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
    }
)
public class User extends AuditorBaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "username", nullable = false, length = 64)
	private String username;

	@Column(name = "email", nullable = false, length = 320)
	private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

	public User() {
	}

	public User(User user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
        this.password = user.getPassword();
	}
}
