package com.draft.restapi.audit.entity;

import javax.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.draft.restapi.auth.entity.User;
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
	private String entityName;

    @Column(name = "entity_id")
	private Integer entityId;

    @Column(name = "operation")
	private String operation;

    @Column(name = "operated_at")
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date operatedAt;

    @JsonIgnoreProperties({"entityLogs", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operated_by_user_id", nullable = false)
	private User operatedBy;

	@OneToMany(mappedBy = "entityLog", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UpdateLog> updateLogs = new HashSet<>();

    public EntityLog(String entityName, Integer entityId, String operation, User operatedBy) {
		this.entityName = entityName;
		this.entityId = entityId;
		this.operation = operation;
		this.operatedBy = operatedBy;
    }
}
