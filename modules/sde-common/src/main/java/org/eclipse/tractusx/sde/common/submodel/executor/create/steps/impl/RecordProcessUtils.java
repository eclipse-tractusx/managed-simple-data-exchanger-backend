package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class RecordProcessUtils {

	public void setFieldValue(ObjectNode rowjObject, String ele, JsonObject jObject, String fieldValue) {

		if (fieldValue == null)
			fieldValue = "";

		fieldValue = fieldValue.trim();

		if (isNumberTypeField(jObject, fieldValue))
			rowjObject.put(ele, Double.parseDouble(fieldValue));
		else if (isDateFormatField(jObject)) {

			if (fieldValue.isBlank())
				fieldValue = null;
			else
				fieldValue = fieldValue.toUpperCase().endsWith("Z") ? fieldValue : fieldValue + "Z";

			rowjObject.put(ele, fieldValue);

		} else
			rowjObject.put(ele, fieldValue);
	}

	private boolean isDateFormatField(JsonObject jObject) {
		return jObject.get("format") != null && "date-time".equals(jObject.get("format").getAsString());
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
