package org.eclipse.tractusx.sde.core.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class SubmodelCsvService {

	private final SubmodelService submodelService;

	private static final List<String> TYPES = List.of("sample", "template");

	@SneakyThrows
	public List<JsonObject> findSubmodelCsv(String submodelName, String type) {

		List<JsonObject> jsonObjectList = new ArrayList<>();
		if (type != null && TYPES.contains(type.toLowerCase())) {

			JsonObject schemaObject = submodelService.findSubmodelByNameAsSubmdelObject(submodelName).getSchema();
			JsonObject asJsonObject = schemaObject.get("items").getAsJsonObject().get("properties").getAsJsonObject();
			List<String> headerList = asJsonObject.keySet().stream().toList();
			JsonObject headerJsonObject = new JsonObject();
			
			headerList.stream().forEach(key -> headerJsonObject.addProperty(key, key));
			
			jsonObjectList.add(headerJsonObject);
			if ("sample".equalsIgnoreCase(type)) {
				JsonObject jsonNode = schemaObject.getAsJsonArray("examples").get(0).getAsJsonObject();
				jsonObjectList.add(jsonNode);
			}
		} else {
			throw new ValidationException("Unknown CSV type: " + type + " for submodel: " + submodelName);
		}
		return jsonObjectList;
	}

}
