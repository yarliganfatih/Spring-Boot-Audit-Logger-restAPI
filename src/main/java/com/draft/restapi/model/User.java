package com.draft.restapi.model;

import javax.persistence.*;
import javax.validation.constraints.*;

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

	public User() {
	}

	public User(User user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
