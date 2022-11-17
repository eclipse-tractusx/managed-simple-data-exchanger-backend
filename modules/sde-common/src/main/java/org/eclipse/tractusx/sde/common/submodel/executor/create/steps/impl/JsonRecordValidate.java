package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import lombok.SneakyThrows;

@Component
public class JsonRecordValidate extends Step {

	JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);

	@SneakyThrows
	public boolean run(Integer rowIndex, JsonNode inputJsonObject) {

		JsonSchema jsonSchema = factory.getSchema(getSubmodelItems().toString());

		Set<ValidationMessage> errors = jsonSchema.validate(inputJsonObject);
		StringBuilder sb = new StringBuilder();
		for (ValidationMessage string : errors) {
			sb.append(string + "\n");
		}
		if (!sb.isEmpty())
			throw new ValidationException(rowIndex + ", " + sb.toString());

		dependentFieldValidation(rowIndex, inputJsonObject);

		return true;

	}

	private void dependentFieldValidation(Integer rowIndex, JsonNode inputJsonObject) {

		JsonObject submodelProperties = getSubmodelDependentRequiredFields();
		Set<String> fields = submodelProperties.keySet();
		for (String ele : fields) {
			try {
				JsonArray jArray = submodelProperties.get(ele).getAsJsonArray();
				String keyFiledValue = inputJsonObject.get(ele).asText();

				if (!StringUtils.isBlank(keyFiledValue) && !keyFiledValue.equals("null")) {

					for (JsonElement dependentField : jArray) {
						String dependentFiledValue = inputJsonObject.get(dependentField.getAsString()).asText();

						if (StringUtils.isBlank(dependentFiledValue) || dependentFiledValue.equals("null"))
							throw new ValidationException(ele + " field is dependent on " + dependentField.getAsString()
									+ ", and dependent field is null or empty");
					}
				}

			} catch (Exception e) {
				throw new ValidationException(rowIndex + ", " + e.toString());
			}
		}
	}
}
