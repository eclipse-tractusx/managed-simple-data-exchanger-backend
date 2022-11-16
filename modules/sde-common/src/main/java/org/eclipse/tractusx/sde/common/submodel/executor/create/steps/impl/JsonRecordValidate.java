package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import java.util.Set;

import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
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

		return true;

	}
}
