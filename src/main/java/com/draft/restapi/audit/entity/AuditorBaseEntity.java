package com.draft.restapi.audit.entity;

import javax.persistence.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuditorBaseEntity {
	private Integer id = 0; // must be assignable to Serializable!

	@JsonIgnore
	private JsonNode jsonObject;

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
