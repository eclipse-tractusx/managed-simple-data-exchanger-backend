package org.eclipse.tractusx.sde.common.mapper;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.common.entities.SubmodelJsonRequest;
import org.mapstruct.Mapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class JsonObjectMapper {

	Gson gson = new Gson();
	@SneakyThrows
	public JsonObject submodelFileRequestToJsonPojo(SubmodelFileRequest submodelFileRequest) {
		return gson.toJsonTree(submodelFileRequest).getAsJsonObject();
	}

	@SneakyThrows
	public JsonObject submodelJsonRequestToJsonPojo(ObjectNode jobj, SubmodelJsonRequest<ObjectNode> submodelJsonRequest) {
		
		JsonObject asJsonObject = gson.fromJson(jobj.toString(), JsonObject.class);
		
		asJsonObject.addProperty("type_of_access", submodelJsonRequest.getTypeOfAccess());
		asJsonObject.add("bpn_numbers", gson.toJsonTree(submodelJsonRequest.getBpnNumbers(), List.class));
		asJsonObject.add("usage_policies", gson.toJsonTree(submodelJsonRequest.getUsagePolicies(), List.class));
		
		return asJsonObject;
	}

}
