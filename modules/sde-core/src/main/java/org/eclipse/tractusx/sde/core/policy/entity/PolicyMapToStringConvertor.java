package org.eclipse.tractusx.sde.core.policy.entity;

import java.util.Collections;
import java.util.Map;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

@Converter
public class PolicyMapToStringConvertor implements AttributeConverter<Map<UsagePolicyEnum, UsagePolicies>, String> {

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	@SneakyThrows
	public String convertToDatabaseColumn(Map<UsagePolicyEnum, UsagePolicies> attribute) {
		String usageList = objectMapper.writeValueAsString(attribute);
		return attribute == null ? null : usageList;
	}

	@Override
	@SneakyThrows
	public Map<UsagePolicyEnum, UsagePolicies> convertToEntityAttribute(String dbData) {
		return dbData == null ? Collections.emptyMap()
				: objectMapper.readValue(dbData, new TypeReference<Map<UsagePolicyEnum, UsagePolicies>>() {
				});
	}
}
