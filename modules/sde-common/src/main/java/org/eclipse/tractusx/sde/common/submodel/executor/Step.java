package org.eclipse.tractusx.sde.common.submodel.executor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Step {

	@Getter
	JsonObject submodelSchema;

	public void init(JsonObject submodelSchema) {
		this.submodelSchema = submodelSchema;
	}

	public JsonObject getSubmodelProperties() {
		return submodelSchema.get("items").getAsJsonObject().get("properties").getAsJsonObject();
	}

	public JsonArray getSubmodelRequiredFields() {
		return submodelSchema.get("items").getAsJsonObject().get("required").getAsJsonArray();
	}

	protected void logDebug(String message) {
		log.debug(String.format("[%s] %s", this.getClass().getSimpleName(), message));
	}
}
