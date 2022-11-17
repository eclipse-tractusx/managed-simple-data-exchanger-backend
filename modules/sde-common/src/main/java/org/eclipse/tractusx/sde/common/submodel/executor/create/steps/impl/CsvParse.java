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

				fieldValue = fieldValue.trim();

				if (jObject.get("format") != null && "date-time".equals(jObject.get("format").getAsString())
						&& fieldValue != null) {
					rowjObject.put(ele, fieldValue.toUpperCase().endsWith("Z") ? fieldValue : fieldValue + "Z");
				} else
					rowjObject.put(ele, fieldValue);

				if (fieldValue != null && !fieldValue.isBlank()) {
					if (jObject.get("type") != null && jObject.get("type").isJsonPrimitive()
							&& "number".equals(jObject.get("type").getAsString())) {
						rowjObject.put(ele, Integer.parseInt(fieldValue));
					} else if (jObject.get("type") != null && jObject.get("type").isJsonArray()) {
						JsonArray types = jObject.get("type").getAsJsonArray();
						for (JsonElement type : types) {
							if ("number".equals(type.getAsString())) {
								rowjObject.put(ele, Integer.parseInt(fieldValue));
							}
						}
					}
				}

				colomnIndex++;

			} catch (ValidationException errorMessages) {
				throw new CsvHandlerUseCaseException(rowData.position(), colomnIndex, errorMessages.toString());
			}
		}

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowData.position(), rowjObject);

		return rowjObject;
	}
}