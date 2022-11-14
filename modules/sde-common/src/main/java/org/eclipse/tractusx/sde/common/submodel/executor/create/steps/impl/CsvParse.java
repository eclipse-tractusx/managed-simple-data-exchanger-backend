package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import java.util.Set;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.validators.SubmodelValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Component
public class CsvParse extends Step {

	@Autowired
	private SubmodelValidator submodelValidator;

	@SneakyThrows
	public JsonObject run(RowData rowData, JsonObject rowjObject, String processId) {

		JsonObject submodelProperties = getSubmodelProperties();
		JsonArray requiredFields = getSubmodelRequiredFields();

		Set<String> fields = submodelProperties.keySet();

		String[] rowDataFields = rowData.content().split(CommonConstants.SEPARATOR, -1);
		if (rowDataFields.length != fields.size()) {
			throw new CsvHandlerUseCaseException(rowData.position(), "This row has the wrong amount of fields");
		}

		int colomnIndex = 0;
		for (String ele : fields) {

			try {

				JsonObject jObject = submodelProperties.get(ele).getAsJsonObject();

				String fieldValue = rowDataFields[colomnIndex];

				fieldValue = fieldValue.trim();

				submodelValidator.validateField(ele, jObject, requiredFields, fieldValue);

				if (fieldValue.isBlank())
					fieldValue = null;

				rowjObject.addProperty(ele, fieldValue);
				colomnIndex++;

			} catch (ValidationException errorMessages) {
				throw new CsvHandlerUseCaseException(rowData.position(), colomnIndex, errorMessages.toString());
			}
		}
		return rowjObject;
	}
}