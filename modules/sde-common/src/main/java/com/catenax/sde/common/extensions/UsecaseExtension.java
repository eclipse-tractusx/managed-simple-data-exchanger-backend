package com.catenax.sde.common.extensions;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;

@Component
public interface UsecaseExtension {

	@SneakyThrows
	default JSONObject loadUsecae(String useCase) {
		JSONObject json = new JSONObject();
		json.put("id", useCase);
		return json;
	}
	
	@SneakyThrows
	default JSONObject discoverAndAddsubmodel(String submodelName) {
		return null;
	}

	
	
	public abstract JSONObject useCase();
}
