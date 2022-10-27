package com.catenax.sde.core.service;

import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.catenax.sde.common.exception.NoDataFoundException;
import com.catenax.sde.core.registry.SubmodelRegistration;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubmodelService {

	private SubmodelRegistration submodelRegistration;

	public Set<String> findAllSubmodels() {
		return submodelRegistration.getModels().keySet();
	}

	public JSONObject findSubmodelByName(String submodelName) {
		return Optional.ofNullable(submodelRegistration.getModels().get(submodelName))
				.orElseThrow(() -> new NoDataFoundException("No data found for " + submodelName));
	}

}
