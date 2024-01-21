package org.eclipse.tractusx.sde.core.policy.entity;

import java.util.Collections;
import java.util.List;

import org.eclipse.tractusx.sde.common.entities.Policies;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

@Converter
public class PoliciesListToStringConverter implements AttributeConverter<List<Policies>, String> {

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	@SneakyThrows
	public String convertToDatabaseColumn(List<Policies> attribute) {
		String policiesList = objectMapper.writeValueAsString(attribute);
		return attribute == null ? null : policiesList;
	}

	@Override
	@SneakyThrows
	public List<Policies> convertToEntityAttribute(String dbData) {
		return dbData == null ? Collections.emptyList()
				: objectMapper.readValue(dbData, new TypeReference<List<Policies>>() {
				});
	}
}
