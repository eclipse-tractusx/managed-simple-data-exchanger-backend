package com.catenax.sde.core.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.catenax.sde.common.exception.NoDataFoundException;
import com.catenax.sde.common.model.Submodel;
import com.catenax.sde.core.registry.SubmodelRegistration;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubmodelService {

	private SubmodelRegistration submodelRegistration;

	public List<Submodel> findAllSubmodels() {
		List<Submodel> ls = new ArrayList<>();
		submodelRegistration.getModels()
				.forEach(obj -> ls.add(Submodel.builder().name(obj.getString("id")).build()));
		return ls;
	}

	public JSONObject findSubmodelByName(String submodelName) {
		return submodelRegistration.getModels().stream().filter(obj -> obj.getString("id").equals(submodelName))
				.findFirst().orElseThrow(() -> new NoDataFoundException("No data found for " + submodelName));
	}

}
