package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import java.util.Set;

import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.validators.SubmodelValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Component
public class JsonRecordValidate extends Step {

	@Autowired
	private SubmodelValidator submodelValidator;

	@SneakyThrows
	public JsonObject run(Integer rowIndex, JsonObject rowjObject, String processId) {

		JsonObject submodelProperties = getSubmodelProperties();
		JsonArray requiredFields = getSubmodelRequiredFields();

		Set<String> fields = submodelProperties.keySet();

		int colomnIndex = 0;

		for (String ele : fields) {
			try {

				JsonObject jObject = submodelProperties.get(ele).getAsJsonObject();

				JsonElement jsonElement = rowjObject.get(ele);
				String fieldValue = null;
				if (!jsonElement.isJsonNull()) {
					fieldValue = jsonElement.getAsString().trim();
				}

				submodelValidator.validateField(ele, jObject, requiredFields, fieldValue);

				colomnIndex++;

			} catch (ValidationException errorMessages) {
				throw new CsvHandlerUseCaseException(rowIndex, colomnIndex, errorMessages.toString());
			}
		}

		rowjObject.addProperty("processId", processId);

		return rowjObject;
	}
}
