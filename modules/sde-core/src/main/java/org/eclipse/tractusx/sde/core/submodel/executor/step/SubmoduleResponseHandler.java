/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.core.submodel.executor.step;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.configuration.properties.SDEConfigurationProperties;
import org.eclipse.tractusx.sde.common.mapper.AspectResponseFactory;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmoduleMapperUsecaseStep;
import org.eclipse.tractusx.sde.core.utils.ValueReplacerUtility;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;

@Service("SubmoduleResponseHandler")
@RequiredArgsConstructor
public class SubmoduleResponseHandler extends Step  implements SubmoduleMapperUsecaseStep {

	private final ValueReplacerUtility valueReplacerUtility;
	private final AspectResponseFactory aspectResponseFactory;
	private final SDEConfigurationProperties sDEConfigurationProperties;

	Gson gson = new Gson();

	@SuppressWarnings("unchecked")
	public JsonObject mapJsonbjectToFormatedResponse(JsonObject jsonObject) {

		jsonObject.addProperty("manufacturerId", sDEConfigurationProperties.getManufacturerId());
		HashMap<String, String> jsonPojoMap = gson.fromJson(jsonObject, HashMap.class);
		String valueReplacer = valueReplacerUtility.valueReplacer(gson.toJson(getResponseTemplateOfModel()),
				jsonPojoMap);

		return aspectResponseFactory.maptoReponse(jsonObject,
				removeNullAndEmptyElementsFromJson(removeNullAndEmptyElementsFromJson(valueReplacer)));
	}

	@SuppressWarnings("deprecation")
	public static String removeNullAndEmptyElementsFromJson(String jsonString) {
		if (jsonString == null) {
			return jsonString;
		}
		try {
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(jsonString);
			cleanByTree(element);
			jsonString = new GsonBuilder().disableHtmlEscaping().create().toJson(element);
			return jsonString;
		} catch (Exception e) {
			return jsonString;
		}
	}

	private static void cleanByTree(JsonElement e1) {
		if (e1 == null || e1.isJsonNull()) {
		} else if (e1.isJsonArray()) {
			for (Iterator<JsonElement> it = e1.getAsJsonArray().iterator(); it.hasNext();) {
				extracted(it);
			}
		} else {
			for (Iterator<Map.Entry<String, JsonElement>> it = e1.getAsJsonObject().entrySet().iterator(); it
					.hasNext();) {
				extracted1(it);
			}
		}
	}

	private static void extracted1(Iterator<Map.Entry<String, JsonElement>> it) {
		Map.Entry<String, JsonElement> eIt = it.next();
		JsonElement e2 = eIt.getValue();
		if (e2 == null || e2.isJsonNull() || (e2.isJsonObject() && e2.getAsJsonObject().entrySet().isEmpty())) {
			it.remove();
		} else if (e2.isJsonArray()) {
			if (e2.getAsJsonArray().size() == 0) {
				it.remove();
			} else {
				cleanByTree(e2);
			}
		} else if (e2.isJsonObject()) {
			cleanByTree(e2);
		} else if (StringUtils.isBlank(e2.getAsString())) {
			it.remove();
		}
	}

	private static void extracted(Iterator<JsonElement> it) {
		JsonElement e2 = it.next();
		if (e2 == null || e2.isJsonNull() || (e2.isJsonObject() && e2.getAsJsonObject().entrySet().isEmpty())) {
			it.remove();
		} else if (e2.isJsonArray()) {
			if (e2.getAsJsonArray().size() == 0) {
				it.remove();
			} else {
				cleanByTree(e2);
			}
		} else if (e2.isJsonObject()) {
			cleanByTree(e2);
		} else if (StringUtils.isBlank(e2.getAsString())) {
			it.remove();
		}
	}

}
