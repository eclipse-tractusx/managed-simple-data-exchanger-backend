package org.eclipse.tractusx.sde.common.mapper;

import java.util.Map;

import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.mapstruct.Mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class JsonObjectMapper {

	Gson gson = new Gson();
	ObjectMapper mapper = new ObjectMapper();

	@SneakyThrows
	public JsonObject submodelFileRequestToJsonPojo(SubmodelFileRequest submodelFileRequest) {
		return gson.toJsonTree(submodelFileRequest).getAsJsonObject();
	}

	@SneakyThrows
	public ObjectNode submodelFileRequestToJsonNodePojo(SubmodelFileRequest submodelFileRequest) {
		return mapper.convertValue(submodelFileRequest, ObjectNode.class);
	}

	@SuppressWarnings("deprecation")
	@SneakyThrows
	public ObjectNode submodelJsonRequestToJsonPojo(ObjectNode jobj, Map<String, Object> mps) {
		jobj.putAll(mapper.convertValue(mps, ObjectNode.class));
		return jobj;
	}

}
