package org.eclipse.tractusx.sde.core.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.core.processreport.repository.SubmodelCustomHistoryGenerator;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class SubmodelCsvService {

	private final SubmodelService submodelService;

	private final SubmodelCustomHistoryGenerator submodelCustomHistoryGenerator;

	private static final List<String> TYPES = List.of("sample", "template");

	@SneakyThrows
	public List<List<String>> findSubmodelCsv(String submodelName, String type) {

		List<List<String>> jsonObjectList = new ArrayList<>();
		if (type != null && TYPES.contains(type.toLowerCase())) {

			JsonObject schemaObject = submodelService.findSubmodelByNameAsSubmdelObject(submodelName).getSchema();
			JsonObject asJsonObject = schemaObject.get("items").getAsJsonObject().get("properties").getAsJsonObject();
			List<String> headerList = asJsonObject.keySet().stream().toList();
			jsonObjectList.add(headerList);

			if ("sample".equalsIgnoreCase(type)) {
				List<String> listexampleValue = new ArrayList<>();
				JsonObject jsonNode = schemaObject.getAsJsonArray("examples").get(0).getAsJsonObject();
				headerList.stream().forEach(key -> listexampleValue.add(jsonNode.get(key).getAsString()));
				jsonObjectList.add(listexampleValue);
			}
		} else {
			throw new ValidationException("Unknown CSV type: " + type + " for submodel: " + submodelName);
		}
		return jsonObjectList;
	}

	@SneakyThrows
	public List<List<String>> findAllSubmodelCsvHistory(String submodel, String processId) {

		List<List<String>> records = new LinkedList<>();
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(submodel);
		JsonObject schemaObject = schemaObj.getSchema();

		List<String> headerName = createCSVColumnHeader(schemaObject);
		records.add(headerName);
		String coloumns = Joiner.on(",").join(headerName);

		String tableName = schemaObj.getProperties().get("tableName");
		if (tableName == null)
			throw new ServiceException("The submodel table name not found for processing");

		records.addAll(submodelCustomHistoryGenerator.findAllSubmodelCsvHistory(coloumns, tableName, processId));

		return records;
	}

	private List<String> createCSVColumnHeader(JsonObject schemaObject) {

		JsonObject asJsonObject = schemaObject.get("items").getAsJsonObject().get("properties").getAsJsonObject();
		List<String> headerList = asJsonObject.keySet().stream().toList();

		List<String> headerName = new LinkedList<>();
		headerName.addAll(headerList);
		headerName.add("shell_id");
		headerName.add("sub_model_id");
		headerName.add("asset_id");
		headerName.add("usage_policy_id");
		headerName.add("access_policy_id");
		headerName.add("contract_defination_id");
		return headerName;
	}

}
