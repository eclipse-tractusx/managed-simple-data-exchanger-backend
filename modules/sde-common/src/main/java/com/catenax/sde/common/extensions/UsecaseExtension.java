package com.catenax.sde.common.extensions;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Component
public interface UsecaseExtension {

	@SneakyThrows
	default JsonObject loadUsecae(String useCase) {
		JsonObject json = new JsonObject();
		json.addProperty("id", useCase);
		return json;
	}
	
	@SneakyThrows
	default JsonObject discoverAndAddsubmodel(String submodelName) {
		return null;
	}

	
	
	public abstract JsonObject useCase();
}
