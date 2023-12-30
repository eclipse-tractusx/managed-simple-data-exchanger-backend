package org.eclipse.tractusx.sde.pcfexchange.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

@Converter
public class PcfJsonToStringConvertor implements AttributeConverter<JsonNode, String> {

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	@SneakyThrows
	public String convertToDatabaseColumn(JsonNode attribute) {
		return attribute == null ? "{}" : attribute.toPrettyString();
	}

	@Override
	@SneakyThrows
	public JsonNode convertToEntityAttribute(String dbData) {
		return dbData == null ? null : objectMapper.readTree(dbData);
	}
}
