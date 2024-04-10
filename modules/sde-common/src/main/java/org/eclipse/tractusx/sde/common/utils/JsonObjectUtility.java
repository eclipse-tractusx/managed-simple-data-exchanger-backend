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
