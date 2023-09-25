package org.eclipse.tractusx.sde.core.policy.entity;

import java.util.Collections;
import java.util.List;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

@Converter
public class PolicyListToStringConvertor implements AttributeConverter<List<UsagePolicies>, String> {

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	@SneakyThrows
	public String convertToDatabaseColumn(List<UsagePolicies> attribute) {
		String usageList = objectMapper.writeValueAsString(attribute);
		return attribute == null ? null : usageList;
	}

	@Override
	@SneakyThrows
	public List<UsagePolicies> convertToEntityAttribute(String dbData) {
		return dbData == null ? Collections.emptyList()
				: objectMapper.readValue(dbData, new TypeReference<List<UsagePolicies>>() {
				});
	}
}
