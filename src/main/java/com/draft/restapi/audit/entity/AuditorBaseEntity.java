package com.draft.restapi.audit.entity;

import javax.persistence.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditorBaseEntity {
	private Integer id;

	@JsonIgnore
	private Integer logId;

	@JsonIgnore
	private EntityLog entityLog;

	@JsonIgnore
	private JsonNode jsonObject;

	public void setJsonObject(JsonNode jsonObject) {
		this.jsonObject = jsonObject;
	}

	public JsonNode transformJsonObject() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
		return mapper.valueToTree(this);
	}

	public String getTableName() {
		Table tableAnnotation = getClass().getAnnotation(Table.class);
		if (tableAnnotation != null) {
			return tableAnnotation.name();
		}
		return null;
	}
}
