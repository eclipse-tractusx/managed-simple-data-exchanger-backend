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

package org.eclipse.tractusx.sde.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonObjectUtility {

	private JsonObjectUtility() {
	}

	public static String getValueFromJsonObjectAsString(JsonNode jsonNode, String field) {
		JsonNode jsonNode2 = jsonNode.get(field);
		if (jsonNode2 == null || jsonNode2.isNull() || jsonNode2.isMissingNode())
			return null;
		else
			return jsonNode2.asText();
	}

	public static Object getValueFromJsonObject(JsonNode jsonNode, String field) {
		JsonNode jsonNode2 = jsonNode.get(field);
		if (jsonNode2 == null || jsonNode2.isNull() || jsonNode2.isMissingNode())
			return null;
		else if (jsonNode2.isDouble())
			return jsonNode2.asDouble();
		else if (jsonNode2.isBoolean())
			return jsonNode2.asBoolean();
		else
			return jsonNode2.asText();
	}

	public static String getValueFromJsonObject(JsonObject jsonNode, String field) {
		JsonElement elememnt = jsonNode.get(field);
		if (elememnt == null || elememnt.isJsonNull())
			return "";
		return elememnt.getAsString();
	}

	public static JsonObject getValueFromJsonObjectAsObject(JsonObject items, String ele) {
		JsonElement elememnt = items.get(ele);
		if (elememnt == null || elememnt.isJsonNull())
			return null;
		return elememnt.getAsJsonObject();
	}

}
