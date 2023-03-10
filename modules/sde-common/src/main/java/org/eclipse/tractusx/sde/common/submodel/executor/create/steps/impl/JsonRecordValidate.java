/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

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
				JsonNode jsonNode = inputJsonObject.get(ele);
				
				String keyFiledValue = null;
				if (!jsonNode.isNull())
					keyFiledValue = jsonNode.asText();

				if (!StringUtils.isBlank(keyFiledValue)) {
					validateDependentFieldValue(inputJsonObject, ele, jArray);
				}

			} catch (Exception e) {
				throw new ValidationException(rowIndex + ", " + e.toString());
			}
		}
	}

	private void validateDependentFieldValue(JsonNode inputJsonObject, String ele, JsonArray jArray) {
		for (JsonElement dependentField : jArray) {
			JsonNode jsonNodeField = inputJsonObject.get(dependentField.getAsString());
			
			String dependentFiledValue = null;
			if (!jsonNodeField.isNull())
				dependentFiledValue = jsonNodeField.asText();

			if (StringUtils.isBlank(dependentFiledValue))
				throw new ValidationException(ele + " field is dependent on " + dependentField.getAsString()
						+ ", and dependent field is null or empty");
		}
	}
}
