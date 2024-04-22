package org.eclipse.tractusx.sde.common.submodel.executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.utils.JsonObjectUtility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface DigitalTwinUsecaseStep {

	public void init(JsonObject submodelSchema);

	public JsonNode run(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy);

	public void delete(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId);

	default void addManufactureIdInSpecificAssetIds(Map<String, String> specificAssetIds, String manufacturerId) {
		specificAssetIds.put(CommonConstants.MANUFACTURER_ID, manufacturerId);
	}

	default String getIdentifier(JsonNode jsonObject, String identifierOfModel) {
		return JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
				extractExactFieldName(identifierOfModel));
	}

	default String generateShortId(JsonNode jsonObject, JsonArray shortIdSpecsOfModel) {
		return shortIdSpecsOfModel.asList().stream().map(ele -> JsonObjectUtility
				.getValueFromJsonObjectAsString(jsonObject, extractExactFieldName(ele.getAsString())))
				.collect(Collectors.joining("_"));
	}

	default Map<String, String> generateSpecificAssetIds(JsonNode jsonObject, JsonObject specificAssetIdsSpecsOfModel) {

		Map<String, String> specIds = new ConcurrentHashMap<>();

		specificAssetIdsSpecsOfModel.entrySet().stream().forEach(entry -> {

			if (entry.getKey().equals("optionalIdentifier")) {

				entry.getValue().getAsJsonArray().forEach(optionaIdentifier -> {
					saveOptionalSpecIds(jsonObject, specIds, optionaIdentifier);
				});

			} else {
				String value = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
						extractExactFieldName(entry.getValue().getAsString()));
				if (StringUtils.isBlank(value)) {
					value = entry.getValue().getAsString();
				}
				specIds.put(entry.getKey(), value);
			}
		});

		return specIds;
	}

	private void saveOptionalSpecIds(JsonNode jsonObject, Map<String, String> specIds, JsonElement optionaIdentifier) {

		String key = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
				extractExactFieldName(optionaIdentifier.getAsJsonObject().get("key").getAsString()));

		String value = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
				extractExactFieldName(optionaIdentifier.getAsJsonObject().get("value").getAsString()));

		if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
			specIds.put(key, value);
		}
	}

	default String extractExactFieldName(String str) {

		if (str.startsWith("${")) {
			return str.replace("${", "").replace("}", "").trim();
		} else {
			return str;
		}
	}

}
