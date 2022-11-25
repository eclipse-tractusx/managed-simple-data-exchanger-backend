package org.eclipse.tractusx.sde.core.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.mapper.SubmodelMapper;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.core.registry.SubmodelRegistration;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubmodelService {

	private final SubmodelRegistration submodelRegistration;

	private final SubmodelMapper submodelMapper;

	public List<Map<String, String>> findAllSubmodels() {

		List<Map<String, String>> ls = new ArrayList<>();

		submodelRegistration.getModels().forEach(obj -> {
			Map<String, String> sbBuild = new LinkedHashMap<>();
			sbBuild.put("id", obj.getId());
			sbBuild.put("name", obj.getName());
			sbBuild.put("version", obj.getVersion());
			sbBuild.put("semanticId", obj.getSemanticId());
			ls.add(sbBuild);
		});
		return ls;
	}

	public Map<Object, Object> findSubmodelByName(String submodelName) {
		return readValue(submodelName).map(e -> submodelMapper.jsonPojoToMap(e.getSchema()))
				.orElseThrow(() -> new NoDataFoundException("No data found for " + submodelName));
	}

	private Optional<Submodel> readValue(String submodelName) {
		return submodelRegistration.getModels().stream()
				.filter(obj -> obj.getId().equalsIgnoreCase(submodelName.toLowerCase())).findFirst();

	}

	public Submodel findSubmodelByNameAsSubmdelObject(String submodelName) {
		return readValue(submodelName).orElseThrow(() -> new ValidationException(submodelName+" submodel is not supported"));
	}

}
