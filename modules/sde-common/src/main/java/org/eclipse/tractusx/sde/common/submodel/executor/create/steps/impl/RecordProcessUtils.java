/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class RecordProcessUtils {

	public void setFieldValue(ObjectNode rowjObject, String ele, JsonObject jObject, String fieldValue,
			boolean isNotNeedToRemoveFromFields) {

		if (fieldValue != null)
			fieldValue = fieldValue.trim();

		if (isNotNeedToRemoveFromFields && StringUtils.isBlank(fieldValue)) {
			// no need to add null field if enum data expected and field is not require
			rowjObject.remove(ele);
		} else if (isNumberTypeField(jObject)) {
			if (StringUtils.isBlank(fieldValue))
				rowjObject.putNull(ele);
			else
				rowjObject.put(ele, Double.parseDouble(fieldValue));
		} else if (isDateFormatField(jObject)) {
			if (StringUtils.isBlank(fieldValue))
				rowjObject.putNull(ele);
			else
				rowjObject.put(ele, fieldValue.toUpperCase().endsWith("Z") ? fieldValue : fieldValue + "Z");
		} else {
			rowjObject.put(ele, fieldValue);
		}

	}

	private boolean isDateFormatField(JsonObject jObject) {
		return jObject.get("format") != null && "date-time".equals(jObject.get("format").getAsString());
	}

	private boolean isNumberTypeField(JsonObject jObject) {
		JsonElement jsonElement = JsonParser.parseString("number");
		if (jObject.get("type") != null && jObject.get("type").isJsonArray()) {
			JsonArray types = jObject.get("type").getAsJsonArray();
			return types.contains(jsonElement);
		}
		return false;

	}

	public boolean isFieldEnumDataExpect(JsonObject jObject) {
		return jObject.get("enum") != null && jObject.get("enum").isJsonArray();
	}

	public boolean isDependentField(String fieldName, JsonObject submodelDependentRequiredFields) {
		Set<String> fields = submodelDependentRequiredFields.keySet();
		return fields.stream().map(ele -> {
			return submodelDependentRequiredFields.get(ele).getAsJsonArray();
		}).filter(ele -> {
			return ele.contains(JsonParser.parseString(fieldName));
		}).findFirst().isEmpty();
	}

}
