package com.catenax.sde.core.registry;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.catenax.sde.common.extensions.SubmodelExtension;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubmodelRegistration {

	private final List<JSONObject> submodelList;

	public SubmodelRegistration() {
		submodelList = new LinkedList<>();
	}

	public void register(SubmodelExtension subomdelService) {
		JSONObject submodel = subomdelService.submodel();
		log.info(submodel.toString());
		submodelList.add(submodel);
	}

	public List<JSONObject> getModels() {
		return this.submodelList;
	}

}
