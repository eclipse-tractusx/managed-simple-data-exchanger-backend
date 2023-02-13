package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import java.util.Set;

import org.eclipse.tractusx.sde.common.exception.JsonRecordHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class JsonRecordFormating extends Step {

	private final RecordProcessUtils recordProcessUtils;

	@SneakyThrows
	public ObjectNode run(Integer rowIndex, ObjectNode rowjObject, String processId) {

		JsonObject submodelProperties = getSubmodelProperties();
		Set<String> fields = submodelProperties.keySet();

		int colomnIndex = 0;
		for (String ele : fields) {
			try {
				JsonObject jObject = submodelProperties.get(ele).getAsJsonObject();

				String fieldValue = null;
				JsonNode jsonValuenode = rowjObject.get(ele);
				if (!jsonValuenode.isNull())
					fieldValue = jsonValuenode.asText();

				recordProcessUtils.setFieldValue(rowjObject, ele, jObject, fieldValue);

				colomnIndex++;

			} catch (ValidationException errorMessages) {
				throw new JsonRecordHandlerUseCaseException(rowIndex, colomnIndex,
						ele + ":" + errorMessages.toString());
			}
		}

		return rowjObject;
	}

}
