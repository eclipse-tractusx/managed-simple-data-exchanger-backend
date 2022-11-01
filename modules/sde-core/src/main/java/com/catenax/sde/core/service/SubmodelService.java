package com.catenax.sde.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.catenax.sde.common.exception.NoDataFoundException;
import com.catenax.sde.common.mapper.SubmodelMapper;
import com.catenax.sde.common.model.Submodel;
import com.catenax.sde.core.registry.SubmodelRegistration;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubmodelService {

	private final SubmodelRegistration submodelRegistration;

	private SubmodelMapper submodelMapper;

	public List<Submodel> findAllSubmodels() {
		List<Submodel> ls = new ArrayList<>();
		submodelRegistration.getModels()
				.forEach(obj -> ls.add(Submodel.builder().name(obj.get("id").getAsString()).build()));
		return ls;
	}

	public Map<Object, Object> findSubmodelByName(String submodelName) {
		return readValue(submodelName).map(e -> submodelMapper.jsonPojoToMap(e))
				.orElseThrow(() -> new NoDataFoundException("No data found for " + submodelName));
	}

	private Optional<JsonObject> readValue(String submodelName) {
		return submodelRegistration.getModels().stream().filter(obj -> obj.get("id").getAsString().toLowerCase().equals(submodelName.toLowerCase()))
				.findFirst();

	}

	public JsonObject findSubmodelByNameAsJsonObject(String submodelName) {
		return readValue(submodelName).orElseThrow(() -> new NoDataFoundException("No data found for " + submodelName));
	}

}
