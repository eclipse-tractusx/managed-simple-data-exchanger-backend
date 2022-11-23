package org.eclipse.tractusx.sde.common.mapper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.tractusx.sde.common.model.Submodel;
import org.mapstruct.Mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class SubmodelMapper {

	Gson gson = new Gson();

	@SneakyThrows
	public JsonObject jsonfileToJsonPojo(InputStream input) {
		Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
		return gson.fromJson(reader, JsonObject.class);
	}

	@SneakyThrows
	public Submodel jsonPojoToSubmodelPojo(JsonObject input) {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(input.toString(), Submodel.class);
	}
	
	@SuppressWarnings("unchecked")
	@SneakyThrows
	public Map<Object,Object> jsonPojoToMap(JsonObject input) {
		return gson.fromJson(input, LinkedHashMap.class);
	}

}
