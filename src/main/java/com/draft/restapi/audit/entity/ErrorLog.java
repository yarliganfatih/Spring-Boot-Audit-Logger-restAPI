package com.draft.restapi.audit.entity;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import com.draft.restapi.auth.entity.User;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "audit_error_logs")
public class ErrorLog implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "endpoint_url")
	private String endpointUrl;

	@Column(name = "http_method")
	private String httpMethod;

	@Column(name = "request_headers", columnDefinition = "TEXT")
	private String requestHeaders;

	@Column(name = "request_params", columnDefinition = "TEXT")
	private String requestParams;

	@Column(name = "request_body", columnDefinition = "TEXT")
	private String requestBody;

	@Column(name = "response_body", columnDefinition = "TEXT")
	private String responseBody;

	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;

	@Column(name = "error_stack_trace", columnDefinition = "TEXT")
	private String errorStackTrace;

	@Column(name = "error_type")
	private String errorType;

	@Column(name = "http_status_code")
	private Integer httpStatusCode;

	@Column(name = "x_trace_id")
	private String xTraceId;

	@Column(name = "occurred_at")
	@Temporal(TemporalType.TIMESTAMP)
	@CreationTimestamp
	private Date occurredAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "occurred_by_user_id", nullable = true)
	private User occurredBy;
}
