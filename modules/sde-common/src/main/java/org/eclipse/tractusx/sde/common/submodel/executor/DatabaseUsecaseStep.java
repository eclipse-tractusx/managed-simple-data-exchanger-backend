package org.eclipse.tractusx.sde.common.submodel.executor;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

public interface DatabaseUsecaseStep {

	public void init(JsonObject submodelSchema);

	public JsonNode run(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy);

	public void saveSubmoduleWithDeleted(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId);

	public List<JsonObject> readCreatedTwins(String processId, String isDeleted);
	
	public JsonObject readCreatedTwinsBySpecifyColomn(String sematicId, String basedCol, String value);

	public JsonObject readCreatedTwinsDetails(String uuid);

	public int getUpdatedData(String processId);
	
	default String extractExactFieldName(String str) {

		if (str.startsWith("${")) {
			return str.replace("${", "").replace("}", "").trim();
		} else {
			return str;
		}
	}

}
