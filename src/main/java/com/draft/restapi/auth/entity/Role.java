package com.draft.restapi.auth.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	@Column(name = "name", unique = true)
	private String name;
	
	@Column(name = "level")
	private Integer level;

	public String getRoleName() {
		return "ROLE_"+name;
	}
}
