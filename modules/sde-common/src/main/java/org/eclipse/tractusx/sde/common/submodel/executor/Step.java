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

	public JsonObject getSubmodelItems() {
		return submodelSchema.get("items").getAsJsonObject();
	}
	
	public JsonObject getSubmodelProperties() {
		return getSubmodelItems().get("properties").getAsJsonObject();
	}

	public JsonArray getSubmodelRequiredFields() {
		return getSubmodelItems().get("required").getAsJsonArray();
	}
	
	public String getIdShortOfModel() {
		return this.submodelSchema.get("idShort").getAsString();
	}
	
	public String getVersionOfModel() {
		return this.submodelSchema.get("version").getAsString();
	}
	
	public String getsemanticIdOfModel() {
		return this.submodelSchema.get("semantic_id").getAsString();
	}
	
	public String getSubmodelDescriptionOfModel() {
		return this.submodelSchema.get("description").getAsString();
	}
	
	public String getSubmodelTitleIdOfModel() {
		return this.submodelSchema.get("title").getAsString();
	}
	
	public JsonObject getSubmodelDependentRequiredFields() {
		return getSubmodelItems().get("dependentRequired").getAsJsonObject();
	}

	protected void logDebug(String message) {
		log.debug(String.format("[%s] %s", this.getClass().getSimpleName(), message));
	}
}
