package com.catenax.sde.core.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.catenax.sde.common.entities.SubmodelFileRequest;
import com.catenax.sde.common.entities.SubmodelJsonRequest;
import com.catenax.sde.core.cvs.entities.CsvContent;
import com.catenax.sde.core.validation.SubmodelCSVValidator;
import com.catenax.sde.core.validation.SumodelValidator;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmodelOrchestartorService {

	private final SubmodelCSVValidator sumodelcsvValidator;

	private final SumodelValidator sumodelValidator;

	private final SubmodelService submodelService;

	public void processSubmodelCsv(CsvContent csvContent,
			SubmodelFileRequest submodelFileRequest, String processId,
			String submodel) {
		boolean validate = sumodelcsvValidator.validate(csvContent, submodel);

		JsonObject submodelSchema = submodelService.findSubmodelByNameAsJsonObject(submodel);
		JsonObject asJsonObject = submodelSchema.get("items").getAsJsonObject().get("properties").getAsJsonObject();
		JsonObject requiredFieldList = submodelSchema.get("items").getAsJsonObject().get("required").getAsJsonObject();
		if (validate) {

			csvContent.getRows().forEach(rowjObj -> {

				// mapper to convert in jsonObject
				
				//sumodelValidator.validate(rowjObj, asJsonObject, requiredFieldList);

				// mappping
				// digital twin
				// edc call
				// databse save call
			});
		}

	}

	public void processSubmodel(SubmodelJsonRequest<JsonObject> submodelJsonRequest, String processId,
			String submodel) {

		JsonObject submodelSchema = submodelService.findSubmodelByNameAsJsonObject(submodel);
		JsonObject asJsonObject = submodelSchema.get("items").getAsJsonObject().get("properties").getAsJsonObject();
		JsonObject requiredFieldList = submodelSchema.get("items").getAsJsonObject().get("required").getAsJsonObject();

		List<JsonObject> rowData = submodelJsonRequest.getRowData();

		rowData.forEach(rowjObj -> {
			sumodelValidator.validate(rowjObj, asJsonObject, requiredFieldList);

			// mappping
			// digital twin
			// edc call
			// databse save call
		});
	}

}
