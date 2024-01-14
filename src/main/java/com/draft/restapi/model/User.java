package com.draft.restapi.model;

import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

    @NotNull
	@Column(name = "username", unique = true)
	private String username;

    @Email
    @NotNull
	@Column(name = "email", unique = true)
	private String email;

    @NotNull
    @Column(name = "password")
    private String password;

	public User() {
	}

	public User(User user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
        this.password = user.getPassword();
	}
}
