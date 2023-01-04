/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.SneakyThrows;

@Component
public class CsvParse extends Step {

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

				setFieldValue(rowjObject, ele, jObject, fieldValue);

				colomnIndex++;

			} catch (ValidationException errorMessages) {
				throw new CsvHandlerUseCaseException(rowData.position(), colomnIndex, errorMessages.toString());
			}
		}

		return rowjObject;
	}

	private void setFieldValue(ObjectNode rowjObject, String ele, JsonObject jObject, String fieldValue) {
		
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