/********************************************************************************
 * Copyright (c) 2022,2024 T-Systems International GmbH
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.common.submodel.executor;

import java.util.Arrays;
import java.util.List;

import org.eclipse.tractusx.sde.common.utils.JsonObjectUtility;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Step {

	@Getter
	JsonObject submodelSchema;

	public void init(JsonObject submodelSchema) {
		this.submodelSchema = submodelSchema;
	}

	public String getNameOfModel() {
		return submodelSchema.get("id").getAsString();
	}

	public JsonObject getSubmodelItems() {
		return submodelSchema.get("items").getAsJsonObject();
	}

	public JsonObject getSubmodelProperties() {
		return getSubmodelItems().get("properties").getAsJsonObject();
	}

	public JsonArray getSubmodelRequiredFields() {
		return getSubmodelItems().get("required").getAsJsonArray();
	}

	public String getIdShortOfModel() {
		return this.submodelSchema.get("idShort").getAsString();
	}

	public String getVersionOfModel() {
		return this.submodelSchema.get("version").getAsString();
	}

	public String getsemanticIdOfModel() {
		return this.submodelSchema.get("semantic_id").getAsString();
	}

	public String getUriPathOfSubmodule() {
		JsonElement jsonElement = this.submodelSchema.get("submodelUriPath");
		return jsonElement == null || jsonElement.isJsonNull() ? "public" : jsonElement.getAsString();
	}
	
	public String getDataPlaneUrlOfSubmodule() {
		JsonElement jsonElement = this.submodelSchema.get("submodelDataPlaneUrl");
		return jsonElement == null || jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
	}
	
	public String getSematicIdReferenceOfSubmodule() {
		JsonElement jsonElement = this.submodelSchema.get("sematicIdReference");
		return jsonElement == null || jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
	}
	
	public String getInterfaceNameOfSubmodule() {
		JsonElement jsonElement = this.submodelSchema.get("interfaceName");
		return jsonElement == null || jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
	}

	public JsonObject getAddOnOfModel() {
		return this.submodelSchema.get("addOn").getAsJsonObject();
	}

	public String getIdentifierOfModel() {
		return this.getAddOnOfModel().get("identifier").getAsString();
	}
	
	public List<String> getDatabaseIdentifierSpecsOfModel() {
		JsonElement jsonElement = this.getAddOnOfModel().get("databaseIdentifierSpecs");
		return jsonElement == null || jsonElement.isJsonNull()
				? List.of(extractExactFieldName(this.getIdentifierOfModel()))
				: converJsonArrayToList(jsonElement);
	}
	
	protected List<String> converJsonArrayToList(JsonElement jsonArray) {
		return jsonArray.getAsJsonArray().asList().stream().map(ele -> extractExactFieldName(ele.getAsString()))
				.toList();
	}

	public boolean checkShellCreateOption() {
		JsonElement jsonElement = this.getAddOnOfModel().get("createShellIfNotExist");
		return jsonElement == null || jsonElement.isJsonNull() || jsonElement.getAsBoolean();
	}
	
	public boolean checkAppendURNUUIDWithIdentifier() {
		JsonElement jsonElement = this.getAddOnOfModel().get("appendURNUUIDWithIdentifier");
		return jsonElement == null || jsonElement.isJsonNull() || jsonElement.getAsBoolean();
	}
	
	public boolean usePCFAssetIdAsDTSubprotocolBodyId() {
		JsonElement jsonElement = this.getAddOnOfModel().get("usePCFAssetIdAsDTSubprotocolBodyId");
		return !(jsonElement == null || jsonElement.isJsonNull()) || (jsonElement !=null && jsonElement.getAsBoolean());
	}
	
	public JsonObject checkIsRelationSubmodel() {
		JsonElement jsonElement = this.getAddOnOfModel().get("isRelationSubmodel");
		return jsonElement == null || jsonElement.isJsonNull() ? null : jsonElement.getAsJsonObject();
	}

	public JsonArray checkIsAutoPopulatedfieldsSubmodel() {
		JsonElement jsonElement = this.getAddOnOfModel().get("autoPopulatedfields");
		return jsonElement == null || jsonElement.isJsonNull() ? null : jsonElement.getAsJsonArray();
	}

	public JsonObject getSpecificAssetIdsSpecsOfModel() {
		return this.getAddOnOfModel().get("lookupShellSpecificAssetIdsSpecs").getAsJsonObject();
	}

	public JsonObject getCreateShellSpecificAssetIdsSpecsOfModel() {
		JsonElement jsonElement = this.getAddOnOfModel().get("createShellSpecificAssetIdsSpecs");
		return jsonElement == null || jsonElement.isJsonNull() ? getSpecificAssetIdsSpecsOfModel()
				: jsonElement.getAsJsonObject();
	}

	public JsonObject getBPNDiscoverySpecsOfModel() {
		return this.getAddOnOfModel().get("bpnDiscoverySpecs").getAsJsonObject();
	}

	public JsonArray getShortIdSpecsOfModel() {
		return this.getAddOnOfModel().get("shortIdSpecs").getAsJsonArray();
	}

	public JsonObject getResponseTemplateOfModel() {
		return this.getAddOnOfModel().get("responseTemplate").getAsJsonObject();
	}

	public String getSubmodelShortDescriptionOfModel() {
		return this.submodelSchema.get("shortDescription").getAsString();
	}

	public String getSubmodelTitleIdOfModel() {
		return this.submodelSchema.get("title").getAsString();
	}

	public JsonObject getSubmodelDependentRequiredFields() {
		return getSubmodelItems().get("dependentRequired").getAsJsonObject();
	}

	protected void logDebug(String message) {
		log.debug(String.format("[%s] %s", this.getClass().getSimpleName(), message));
	}

	protected void logInfo(String message) {
		log.info(String.format("[%s] %s", this.getClass().getSimpleName(), message));
	}
	
	public String getIdentifier(JsonNode jsonObject, String identifierOfModel) {
		return JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
				extractExactFieldName(identifierOfModel));
	}
	
	protected String getDatabaseIdentifierValues(JsonNode jsonObject, List<String> databaseIdentifier) {
		return String.join("@",
				databaseIdentifier.stream().map(
						str -> JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject, extractExactFieldName(str)))
						.toList());
	}
	
	protected List<String> getIdentifierValuesAsList(String value) {
		return Arrays.asList(value.split("@")).stream().map(Object::toString).toList();
	}
	
	public String extractExactFieldName(String str) {

		if (str.startsWith("${")) {
			return str.replace("${", "").replace("}", "").trim();
		} else {
			return str;
		}
	}
}