package com.catenax.sde.core.validation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.catenax.sde.core.cvs.entities.CsvContent;
import com.catenax.sde.core.service.SubmodelService;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmodelCSVValidator {

	private final SubmodelService submodelService;

	public boolean validate(CsvContent csvContent, String submodel) {
		JsonObject submodelSchema = submodelService.findSubmodelByNameAsJsonObject(submodel);
		JsonObject asJsonObject = submodelSchema.get("items").getAsJsonObject().get("properties").getAsJsonObject();
		List<String> columns = csvContent.getColumns();
		Set<String> keySet = asJsonObject.keySet();
		Set<String> targetSet = new LinkedHashSet<>(columns);
		return keySet.equals(targetSet);
	}

}
