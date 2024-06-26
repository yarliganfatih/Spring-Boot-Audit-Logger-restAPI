package com.draft.restapi.audit.entity;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.draft.restapi.auth.entity.SignedUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "audit_entity_logs")
public class EntityLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

    @Column(name = "entity_name")
	private String entity_name;
	
    @Column(name = "entity_id")
	private Integer entity_id;
	
    @Column(name = "operation")
	private String operation;
	
    @Column(name = "operated_at")
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private java.util.Date operated_at;

    @JsonIgnoreProperties("entityLogs")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operated_by_user_id", nullable = false)
	private SignedUser operated_by;

	@OneToMany(mappedBy = "entityLog", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UpdateLog> updateLogs = new HashSet<>();

    public EntityLog(String entity_name, Integer entity_id, String operation, SignedUser operated_by) {
		this.entity_name = entity_name;
		this.entity_id = entity_id;
		this.operation = operation;
		this.operated_by = operated_by;
    }
}
