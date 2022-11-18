package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import java.util.Set;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.SneakyThrows;

@Component
public class CsvParse extends Step {

	@Autowired
	private JsonRecordValidate jsonRecordValidate;

	@SneakyThrows
	public ObjectNode run(RowData rowData, ObjectNode rowjObject, String processId) {

		JsonObject submodelProperties = getSubmodelProperties();

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

				if (fieldValue == null)
					fieldValue = "";

				fieldValue = fieldValue.trim();

				if (isNumberTypeField(jObject, fieldValue))
					rowjObject.put(ele, Integer.parseInt(fieldValue));
				else if (isDateFormatField(jObject, fieldValue)) {
					rowjObject.put(ele, fieldValue.toUpperCase().endsWith("Z") ? fieldValue : fieldValue + "Z");
				} else
					rowjObject.put(ele, fieldValue);

				colomnIndex++;

			} catch (ValidationException errorMessages) {
				throw new CsvHandlerUseCaseException(rowData.position(), colomnIndex, errorMessages.toString());
			}
		}

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowData.position(), rowjObject);

		return rowjObject;
	}

	private boolean isDateFormatField(JsonObject jObject, String fieldValue) {
		return jObject.get("format") != null && "date-time".equals(jObject.get("format").getAsString())
				&& fieldValue != null;
	}

	private boolean isNumberTypeField(JsonObject jObject, String fieldValue) {

		if (fieldValue != null && !fieldValue.isBlank() && jObject.get("type") != null
				&& jObject.get("type").isJsonArray()) {
			JsonArray types = jObject.get("type").getAsJsonArray();
			JsonElement jsonElement = JsonParser.parseString("number");
			return types.contains(jsonElement);
		}
		return false;

	}
}