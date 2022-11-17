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
				if (fieldValue.isBlank())
					fieldValue = null;

				if (jObject.get("type") != null && "number".equals(jObject.get("type").getAsString())
						&& fieldValue != null)
					rowjObject.put(ele, Integer.parseInt(fieldValue));
				else
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
}