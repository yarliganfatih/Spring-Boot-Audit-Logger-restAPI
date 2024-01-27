package com.draft.restapi.audit.entity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "audit_update_logs")
public class UpdateLog implements Serializable {

	private static final long serialVersionUID = 2L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="log_id", nullable = false)
	private EntityLog entityLog;

	@Column(name = "op")
	private String op;

	@Column(name = "path")
	private String path;
	
	@Column(name = "previous_value")
	private String previousValue;
	
    public UpdateLog() {
    }

    public UpdateLog(EntityLog entityLog, String op, String path, String previousValue) {
		this.entityLog = entityLog;
		this.op = op;
		this.path = path;
		this.previousValue = previousValue;
    }
}
