package org.eclipse.tractusx.sde.core.submodel.executor.step;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.configuration.properties.SDEConfigurationProperties;
import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.constants.SubmoduleCommonColumnsConstant;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.JsonObjectUtility;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class DigitalTwinUseCaseHandler extends Step {

	private final DigitalTwinsFacilitator digitalTwinFacilitator;

	private final DigitalTwinsUtility digitalTwinsUtility;

	private final SDEConfigurationProperties sdeConfigProperties;

	@SneakyThrows
	public JsonNode run(JsonNode jsonObject, PolicyModel policy) {

		String identifier = getIdentifier(jsonObject);
		String shortIdForShell = generateShortId(jsonObject);
		Map<String, String> specificAssetIds = generateSpecificAssetIds(jsonObject);

		addManufactureIdInSpecificAssetIds(specificAssetIds);

		ShellDescriptorRequest aasDescriptorRequest = digitalTwinsUtility.getShellDescriptorRequest(shortIdForShell,
				identifier, specificAssetIds, policy);

		String shellId = checkAndCreateShellIfNotExist(specificAssetIds, aasDescriptorRequest);

		((ObjectNode) jsonObject).put(SubmoduleCommonColumnsConstant.SHELL_ID, shellId);

		Map<String, String> submodelId = checkAndCreateSubmodulIfNotExist(aasDescriptorRequest, shellId);

		submodelId.entrySet().forEach(entry -> ((ObjectNode) jsonObject).put(entry.getKey(), entry.getValue()));

		return jsonObject;
	}

	private String getIdentifier(JsonNode jsonObject) {
		return JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject, getIdentifierOfModel());
	}

	private String generateShortId(JsonNode jsonObject) {
		return getShortIdSpecsOfModel().asList().stream()
				.map(ele -> JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject, ele.getAsString()))
				.collect(Collectors.joining("_"));
	}

	private Map<String, String> generateSpecificAssetIds(JsonNode jsonObject) {

		Map<String, String> specIds = new ConcurrentHashMap<>();

		getSpecificAssetIdsSpecsOfModel().entrySet().stream().forEach(entry -> {

			if (entry.getKey().equals("optionalIdentifier")) {

				entry.getValue().getAsJsonArray().forEach(optionaIdentifier -> {
					
					String key = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
							optionaIdentifier.getAsJsonObject().get("key").getAsString());
					String value = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
							optionaIdentifier.getAsJsonObject().get("value").getAsString());
					
					if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value))
						specIds.put(key, value);
					
				});

			} else {
				String value = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
						entry.getValue().getAsString());
				if (StringUtils.isBlank(value)) {
					value = entry.getValue().getAsString();
				}
				specIds.put(entry.getKey(), value);
			}
		});

		return specIds;
	}

	@SneakyThrows
	public String checkAndCreateShellIfNotExist(Map<String, String> specificAssetIds,
			ShellDescriptorRequest aasDescriptorRequest) throws CsvHandlerDigitalTwinUseCaseException {

		String shellId = null;

		addManufactureIdInSpecificAssetIds(specificAssetIds);

		ShellLookupRequest shellLookupRequest = digitalTwinsUtility.getShellLookupRequest(specificAssetIds);
		List<String> shellIds = digitalTwinFacilitator.shellLookup(shellLookupRequest);

		if (shellIds.isEmpty()) {
			logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
			ShellDescriptorResponse result = digitalTwinFacilitator.createShellDescriptor(aasDescriptorRequest);
			shellId = result.getIdentification();
			logDebug(String.format("Shell created with id '%s'", shellId));
		} else if (shellIds.size() == 1) {
			logDebug(String.format("Shell id found for '%s'", shellLookupRequest.toJsonString()));
			shellId = shellIds.stream().findFirst().orElse(null);
			logDebug(String.format("Shell id '%s'", shellId));
		} else {
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple ids found on aspect %s", shellLookupRequest.toJsonString()));
		}
		return shellId;
	}

	public Map<String, String> checkAndCreateSubmodulIfNotExist(ShellDescriptorRequest aasDescriptorRequest,
			String shellId) {

		Map<String, String> twinInfo = new ConcurrentHashMap<>();

		SubModelListResponse subModelResponse = digitalTwinFacilitator.getSubModels(shellId);
		SubModelResponse foundSubmodel = null;
		if (subModelResponse != null) {
			foundSubmodel = subModelResponse.getResult().stream()
					.filter(x -> getIdShortOfModel().equals(x.getIdShort())).findFirst().orElse(null);
		}

		if (subModelResponse == null || foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			CreateSubModelRequest createSubModelRequest = digitalTwinsUtility.getCreateSubModelRequest(shellId,
					getsemanticIdOfModel(), getIdShortOfModel(), getNameOfModel(), "",
					getSubmodelShortDescriptionOfModel());

			digitalTwinFacilitator.updateShellDetails(shellId, aasDescriptorRequest, createSubModelRequest);
			twinInfo.put(SubmoduleCommonColumnsConstant.SUBMODULE_ID, createSubModelRequest.getId());

		} else {
			// There is no need to send submodel because of nothing to change in it so
			// sending null of it
			twinInfo.put(SubmoduleCommonColumnsConstant.SUBMODULE_ID, foundSubmodel.getId());
			digitalTwinFacilitator.updateShellDetails(shellId, aasDescriptorRequest, null);
			twinInfo.put(SubmoduleCommonColumnsConstant.UPDATED, CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}

		return twinInfo;
	}

	private void addManufactureIdInSpecificAssetIds(Map<String, String> specificAssetIds) {
		specificAssetIds.put(CommonConstants.MANUFACTURER_ID, sdeConfigProperties.getManufacturerId());
	}

	@SneakyThrows
	public ShellLookupRequest getShellLookupRequest(Map<String, String> specificAssetIds) {
		addManufactureIdInSpecificAssetIds(specificAssetIds);
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

	// public ShellDescriptorResponse createShellDescriptor(ShellDescriptorRequest
	// aasDescriptorRequest) {
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

	public void deleteSubmodelfromShellById(JsonObject jsonObject) {
		String shellId = JsonObjectUtility.getValueFromJsonObject(jsonObject, SubmoduleCommonColumnsConstant.SHELL_ID);
		String submodelId = JsonObjectUtility.getValueFromJsonObject(jsonObject,
				SubmoduleCommonColumnsConstant.SUBMODULE_ID);
		digitalTwinFacilitator.deleteSubmodelfromShellById(shellId, submodelId);
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

		addManufactureIdInSpecificAssetIds(specificAssetIds);

		return digitalTwinsUtility.getShellDescriptorRequest(nameAtManufacturer, manufacturerPartId, uuid,
				specificAssetIds, policy);
	}

}
