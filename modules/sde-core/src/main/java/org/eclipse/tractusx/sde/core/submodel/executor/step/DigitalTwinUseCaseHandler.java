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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.configuration.properties.PCFAssetStaticPropertyHolder;
import org.eclipse.tractusx.sde.common.configuration.properties.SDEConfigurationProperties;
import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.constants.SubmoduleCommonColumnsConstant;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.DigitalTwinUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.JsonObjectUtility;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.Endpoint;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service("digitalTwinUseCaseHandler")
@RequiredArgsConstructor
public class DigitalTwinUseCaseHandler extends Step implements DigitalTwinUsecaseStep {
	
	private static final String FORWARD_SLASH = "/";

	private final DigitalTwinsFacilitator digitalTwinFacilitator;

	private final DigitalTwinsUtility digitalTwinsUtility;

	private final SDEConfigurationProperties sdeConfigProperties;

	private final DigitalTwinLookUpInRegistry digitalTwinLookUpInRegistry;
	
	private final DigitalTwinAccessRuleFacilator digitalTwinAccessRuleFacilator;
	
	private final PCFAssetStaticPropertyHolder pcfAssetStaticPropertyHolder;
	
	@Value(value = "${edc.hostname}${edc.dataplane.endpointpath:/api/public}")
	public String digitalTwinEdcDataplaneEndpoint;
	
	public void delete(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId) {
		String shellId = JsonObjectUtility.getValueFromJsonObject(jsonObject, SubmoduleCommonColumnsConstant.SHELL_ID);
		String submodelId = JsonObjectUtility.getValueFromJsonObject(jsonObject,
				SubmoduleCommonColumnsConstant.SUBMODULE_ID);
		digitalTwinFacilitator.deleteSubmodelfromShellById(shellId, submodelId);
	}

	@SneakyThrows
	public JsonNode run(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy) {
		try {
			String identifier = getIdentifier(jsonObject, getIdentifierOfModel());

			String shortIdForShell = generateShortId(jsonObject, getShortIdSpecsOfModel());

			Map<String, String> specificAssetIds = generateSpecificAssetIds(jsonObject,
					getSpecificAssetIdsSpecsOfModel());

			addManufactureIdInSpecificAssetIds(specificAssetIds, sdeConfigProperties.getManufacturerId());

			ShellDescriptorRequest aasDescriptorRequest = digitalTwinsUtility.getShellDescriptorRequest(shortIdForShell,
					identifier, specificAssetIds, policy);

			String shellId = checkShellAndGetIdIfExist(jsonObject, specificAssetIds);

			if (StringUtils.isBlank(shellId)) {
				specificAssetIds = generateSpecificAssetIds(jsonObject, getCreateShellSpecificAssetIdsSpecsOfModel());
				addManufactureIdInSpecificAssetIds(specificAssetIds, sdeConfigProperties.getManufacturerId());
				aasDescriptorRequest = digitalTwinsUtility.getShellDescriptorRequest(shortIdForShell, identifier,
						specificAssetIds, policy);
				createShell(specificAssetIds, aasDescriptorRequest);
				shellId = aasDescriptorRequest.getId();
			}

			jsonObject.put(SubmoduleCommonColumnsConstant.SHELL_ID, shellId);

			SubModelResponse foundSubmodel = findSubmoduleInShells(jsonObject, List.of(shellId));

			checkAndCreateSubmodulIfNotExist(rowIndex, jsonObject, shellId, aasDescriptorRequest, foundSubmodel);
			
			digitalTwinAccessRuleFacilator.init(getSubmodelSchema());
			digitalTwinAccessRuleFacilator.createAccessRule(rowIndex, jsonObject, specificAssetIds, policy, getsemanticIdOfModel());

		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(rowIndex, ": DigitalTwins: " + e.getMessage());
		}

		return jsonObject;
	}

	@SneakyThrows
	public String checkShellAndGetIdIfExist(ObjectNode jsonObject, Map<String, String> specificAssetIds)
			throws CsvHandlerDigitalTwinUseCaseException {

		String shellId = null;

		ShellLookupRequest shellLookupRequest = digitalTwinsUtility.getShellLookupRequest(specificAssetIds);
		List<String> shellIds = digitalTwinFacilitator.shellLookup(shellLookupRequest);

		if (shellIds.isEmpty()) {
			logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
		} else if (shellIds.size() == 1) {
			logDebug(String.format("Shell id found for '%s'", shellLookupRequest.toJsonString()));
			shellId = shellIds.stream().findFirst().orElse(null);
			logDebug(String.format("Shell id '%s'", shellId));
		} else if (shellIds.size() > 1) {
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple ids found on aspect %s", shellLookupRequest.toJsonString()));
		}

		return shellId;
	}

	@SneakyThrows
	private String createShell(Map<String, String> specificAssetIds, ShellDescriptorRequest aasDescriptorRequest) {

		String shellId = null;
		ShellLookupRequest shellLookupRequest = digitalTwinsUtility.getShellLookupRequest(specificAssetIds);

		if (checkShellCreateOption()) {
			ShellDescriptorResponse result = digitalTwinFacilitator.createShellDescriptor(aasDescriptorRequest);
			shellId = result.getIdentification();
			logDebug(String.format("Shell created with id '%s'", shellId));
		} else {
			// We don't need to create parent shell for aspect relationship because
			// checkShellCreateOption=false
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("No shell found in DT %s", shellLookupRequest.toJsonString()));
		}

		return shellId;

	}

	public JsonNode checkAndCreateSubmodulIfNotExist(Integer rowIndex, ObjectNode jsonObject, String shellId,
			ShellDescriptorRequest aasDescriptorRequest, SubModelResponse foundSubmodel) {

		Map<String, String> identification = findIdentificationForSubmodule(rowIndex, jsonObject, foundSubmodel);

		String path = getUriPathOfSubmodule();
		String submodelDataPlaneUrl = getDataPlaneUrlOfSubmodule();
		
		String submodelRequestidentifier= identification.get("submodelRequestidentifier");
		String submodelIdentifier = identification.get("submodelIdentifier");
		
		if (StringUtils.isNotBlank(path)) {
			path =  FORWARD_SLASH + path + FORWARD_SLASH+ submodelRequestidentifier;
		}
		
		if (StringUtils.isBlank(submodelDataPlaneUrl)) {
			submodelDataPlaneUrl = digitalTwinEdcDataplaneEndpoint;
		}
		
		String endPointAddress= submodelDataPlaneUrl + path;
		
		String edcAssetId = shellId + "-" + submodelIdentifier;
		
		if (usePCFAssetIdAsDTSubprotocolBodyId())
			edcAssetId = pcfAssetStaticPropertyHolder.getPcfExchangeAssetId();
		
		
		String sematicIdReference = getSematicIdReferenceOfSubmodule();
		
		String interfaceName = getInterfaceNameOfSubmodule();
		
		CreateSubModelRequest createSubModelRequest = digitalTwinsUtility.getCreateSubModelRequest(shellId,
				getsemanticIdOfModel(), getIdShortOfModel(), submodelIdentifier, edcAssetId, endPointAddress,
				getSubmodelShortDescriptionOfModel(), sematicIdReference, interfaceName);
		
		if (foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			digitalTwinFacilitator.updateShellDetails(shellId, aasDescriptorRequest, createSubModelRequest);
			jsonObject.put(SubmoduleCommonColumnsConstant.SUBMODULE_ID, createSubModelRequest.getId());

		} else {
			boolean isSubmodelRequestidentifierSame = false;
			if(foundSubmodel.getEndpoints()!=null) {
				isSubmodelRequestidentifierSame = foundSubmodel.getEndpoints().stream()
						.map(Endpoint::getProtocolInformation)
						.anyMatch(ele-> ele.getEndpointAddress().contains(submodelRequestidentifier));
			}
			
			if(!(foundSubmodel.getId().equals(submodelIdentifier)) || !isSubmodelRequestidentifierSame ) {
				logDebug(String.format("Found submodel but need to update submodels for '%s'", shellId));
				digitalTwinFacilitator.updateShellDetails(shellId, aasDescriptorRequest, createSubModelRequest);
				jsonObject.put(SubmoduleCommonColumnsConstant.SUBMODULE_ID, createSubModelRequest.getId());
			} else {
				jsonObject.put(SubmoduleCommonColumnsConstant.SUBMODULE_ID, foundSubmodel.getId());
				// There is no need to send submodel because of nothing to change in it so
				// sending null of it
				digitalTwinFacilitator.updateShellDetails(shellId, aasDescriptorRequest, null);
				logDebug("Complete Digital Twins Update Update Digital Twins");
			}
		}

		return jsonObject;
	}

	private SubModelResponse findSubmoduleInShells(ObjectNode jsonObject, List<String> shellIds) {

		String shellGlobalassetId = null;
		SubModelResponse foundSubmodel = null;

		List<ShellDescriptorResponse> shellDescriptorResponseList = digitalTwinFacilitator
				.getShellDescriptorsWithSubmodelDetails(shellIds);

		for (ShellDescriptorResponse shellDescriptorResponse : shellDescriptorResponseList) {
			shellGlobalassetId = shellDescriptorResponse.getGlobalAssetId();
			foundSubmodel = shellDescriptorResponse.getSubmodelDescriptors().stream()
					.filter(x -> getIdShortOfModel().equals(x.getIdShort())).findFirst().orElse(null);
		}

		if (foundSubmodel != null) {
			jsonObject.put(SubmoduleCommonColumnsConstant.UPDATED, CommonConstants.UPDATED_Y);
		}

		JsonArray jArray = checkIsAutoPopulatedfieldsSubmodel();

		if (jArray != null) {
			List<JsonElement> allGlobalFiled = jArray.asList().stream()
					.filter(ele -> {
						JsonElement refElement = ele.getAsJsonObject().get("ref");
						return refElement != null && refElement.isJsonPrimitive()
								&& refElement.getAsJsonPrimitive().isString()
								&& refElement.getAsJsonPrimitive().getAsString().equals("shellGlobalAssetId");
					})
					.toList();
			if (!allGlobalFiled.isEmpty()) {
				for (JsonElement jsonElement : allGlobalFiled) {
					jsonObject.put(extractExactFieldName(jsonElement.getAsJsonObject().get("key").getAsString()),
							shellGlobalassetId);
				}
			}
		}

		return foundSubmodel;
	}

	@SneakyThrows
	private Map<String, String> findIdentificationForSubmodule(Integer rowIndex, ObjectNode jsonObject, SubModelResponse foundSubmodel) {
		
		String submodelIdentifier =null;
		String identificationField = extractExactFieldName(getIdentifierOfModel());
		
		List<String> databaseIdentifierFields = getDatabaseIdentifierSpecsOfModel();
		
		JsonArray jArray = checkIsAutoPopulatedfieldsSubmodel();
		List<JsonElement> allAutoPopulateField = null;

		if (jArray != null) {
			allAutoPopulateField = jArray.asList().stream().filter(ele -> {
				JsonElement ref = ele.getAsJsonObject().get("ref");
				return ref != null && ref.isJsonObject();
			}).toList();

		}
		if (allAutoPopulateField != null && !allAutoPopulateField.isEmpty()) {

			for (JsonElement jsonElement : allAutoPopulateField) {
				JsonObject asJsonObject = jsonElement.getAsJsonObject().get("ref").getAsJsonObject();
				String shemaIdentificationKey = extractExactFieldName(jsonElement.getAsJsonObject().get("key").getAsString());

				String identificationLocal = findIdentificationForSubmodule(rowIndex, jsonObject, asJsonObject);
				jsonObject.put(shemaIdentificationKey, identificationLocal);
				
				if (identificationField.equals(shemaIdentificationKey)) {
					submodelIdentifier = identificationLocal;
				}
			}
		} 

		if(foundSubmodel != null)
			submodelIdentifier = foundSubmodel.getId();

		if(submodelIdentifier == null) {
			submodelIdentifier = UUIdGenerator.getUrnUuid();
		}
		
		String submodelRequestidentifier = getDatabaseIdentifierValues(jsonObject, databaseIdentifierFields);
		
		Map<String, String> output= new TreeMap<>();
		output.put("submodelIdentifier", submodelIdentifier);
		output.put("submodelRequestidentifier", submodelRequestidentifier);
		
		return output;
	}

	private String findIdentificationForSubmodule(Integer rowIndex, ObjectNode jsonObject, JsonObject asJsonObject) {

		if (asJsonObject != null && asJsonObject.isJsonObject()) {
			boolean lookupChildTwinRemote = asJsonObject.get("lookupChildTwinRemote").getAsBoolean();

			Map<String, String> specificAssetIds = generateSpecificAssetIds(jsonObject,
					asJsonObject.get("specificAssetIdsSpecs").getAsJsonObject());

			if (lookupChildTwinRemote) {

				String bpnForRemoteRegistry = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
						extractExactFieldName(
								JsonObjectUtility.getValueFromJsonObject(asJsonObject, "bpn_for_remote_registry")));

				return digitalTwinLookUpInRegistry.lookupTwinRemotely(rowIndex, specificAssetIds, bpnForRemoteRegistry,
						jsonObject, asJsonObject);
			} else {
				addManufactureIdInSpecificAssetIds(specificAssetIds, sdeConfigProperties.getManufacturerId());
				return digitalTwinLookUpInRegistry.lookupTwinInLocalRrgistry(rowIndex, specificAssetIds, jsonObject,
						asJsonObject);
			}
		} else {
			return UUIdGenerator.getUrnUuid();
		}
	}

	@SneakyThrows
	public ShellLookupRequest getShellLookupRequest(Map<String, String> specificAssetIds) {
		addManufactureIdInSpecificAssetIds(specificAssetIds, sdeConfigProperties.getManufacturerId());
		return digitalTwinsUtility.getShellLookupRequest(specificAssetIds);
	}

	@SneakyThrows
	public ShellLookupRequest getShellLookupRequestWithOutManufactureId(Map<String, String> specificAssetIds) {
		return digitalTwinsUtility.getShellLookupRequest(specificAssetIds);
	}

	@SneakyThrows
	public List<String> shellLookup(ShellLookupRequest shellLookupRequest) {
		return digitalTwinFacilitator.shellLookup(shellLookupRequest);
	}

// 	public ShellDescriptorResponse createShellDescriptor(ShellDescriptorRequest aasDescriptorRequest) {
//		return digitalTwinFacilitator.createShellDescriptor(aasDescriptorRequest);
//	}
//
//	public void updateShellSpecificAssetIdentifiers(String shellId, List<Object> specificAssetIds) {
//		digitalTwinFacilitator.updateShellSpecificAssetIdentifiers(shellId, specificAssetIds);
//	}
//
//	public SubModelListResponse getSubModels(String shellId) {
//		return digitalTwinFacilitator.getSubModels(shellId);
//	}
//
	public void createSubModel(String shellId, CreateSubModelRequest request) {
		digitalTwinFacilitator.createSubModel(shellId, request);
	}

	public void updateSubModel(String shellId, String existingId, CreateSubModelRequest createSubModelRequest) {
		digitalTwinFacilitator.updateSubModel(shellId, existingId, createSubModelRequest);
	}

	public List<ShellDescriptorResponse> getShellDescriptorsWithSubmodelDetails(List<String> shellIds) {
		return digitalTwinFacilitator.getShellDescriptorsWithSubmodelDetails(shellIds);
	}
//
//	public ShellDescriptorResponse getShellDetailsById(String shellId) {
//		return digitalTwinFacilitator.getShellDetailsById(shellId);
//	}
//

	public ShellDescriptorRequest getShellDescriptorRequest(String nameAtManufacturer, String manufacturerPartId,
			String uuid, Map<String, String> specificAssetIds, PolicyModel policy) {

		addManufactureIdInSpecificAssetIds(specificAssetIds, sdeConfigProperties.getManufacturerId());

		return digitalTwinsUtility.getShellDescriptorRequest(nameAtManufacturer, manufacturerPartId, uuid,
				specificAssetIds, policy);
	}

}