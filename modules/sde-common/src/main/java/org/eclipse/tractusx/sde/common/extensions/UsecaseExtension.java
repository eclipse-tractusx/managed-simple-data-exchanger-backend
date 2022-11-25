package org.eclipse.tractusx.sde.common.extensions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

public abstract class UsecaseExtension {

	private JsonObject json;

	@SneakyThrows
	public JsonObject loadUsecae(String useCase) {
		json.addProperty("id", useCase);
		return json;
	}

	public void addSubmodel(String submodelName) {
		JsonArray asJsonObject = json.get("submdel").getAsJsonArray();
		if (asJsonObject == null)
			asJsonObject = new JsonArray();
		asJsonObject.add(submodelName);
	}

	public abstract JsonObject getUseCase();
}
