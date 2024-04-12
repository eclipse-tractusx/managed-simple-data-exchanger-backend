package org.eclipse.tractusx.sde.common.submodel.executor;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

public interface EDCUsecaseStep {

	public void init(JsonObject submodelSchema);
	
	public JsonNode run(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy);

	public void delete(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId);

}
