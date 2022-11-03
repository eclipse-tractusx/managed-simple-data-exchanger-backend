package com.catenax.sde.core.registry;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.catenax.sde.common.extensions.SubmodelExtension;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubmodelRegistration {

	private final List<JsonObject> submodelList;


	public SubmodelRegistration() {
		submodelList = new LinkedList<>();
	}

	public void register(SubmodelExtension subomdelService) {
		JsonObject submodel = subomdelService.submodel();
		log.info(submodel.toString());
		submodelList.add(submodel);
	}

	public List<JsonObject> getModels() {
		return this.submodelList;
	}

}
