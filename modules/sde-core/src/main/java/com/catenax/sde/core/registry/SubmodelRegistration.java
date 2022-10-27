package com.catenax.sde.core.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.catenax.sde.common.extensions.SubmodelExtension;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubmodelRegistration {

	private final Map<String, JSONObject> submodelList;

	public SubmodelRegistration() {
		submodelList = new ConcurrentHashMap<>();
	}

	public void register(SubmodelExtension subomdelService) {
		JSONObject submodel = subomdelService.submodel();
		log.info(submodel.toString());
		submodelList.put(submodel.getString("id"), submodel);
	}

	public Map<String, JSONObject> getModels() {
		return this.submodelList;
	}

}
