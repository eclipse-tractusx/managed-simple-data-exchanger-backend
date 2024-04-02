/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
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

package org.eclipse.tractusx.sde.submodels.apr.steps;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.EDCDigitalTwinProxyForLookUp;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.EDCAssetUrlCacheService;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalTwinsAspectRelationShipCsvHandlerUseCase extends Step {

	private final DigitalTwinsFacilitator digitalTwinfacilitaor;
	private final DigitalTwinsUtility digitalTwinsUtility;
	private final EDCAssetUrlCacheService edcAssetUrlCacheService;
	private final EDCDigitalTwinProxyForLookUp eDCDigitalTwinProxyForLookUp;

	@Value("${digital-twins.managed.thirdparty:false}")
	private boolean dDTRManagedThirdparty;

	@Value("${digital-twins.registry.uri:}")
	private String registryUri;

	@Value("${digital-twins.registry.lookup.uri:}")
	private String registryLookupUri;

	@SneakyThrows
	public AspectRelationship run(AspectRelationship aspectRelationShip, PolicyModel policy)
			throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(aspectRelationShip, policy);
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					": DigitalTwins: " + e.getMessage());
		}
	}

	@SneakyThrows
	private AspectRelationship doRun(AspectRelationship aspectRelationShip, PolicyModel policy)
			throws CsvHandlerUseCaseException, CsvHandlerDigitalTwinUseCaseException {

		ShellLookupRequest shellLookupRequest = digitalTwinsUtility
				.getShellLookupRequest(getSpecificAssetIds(aspectRelationShip));
		List<String> shellIds = digitalTwinfacilitaor.shellLookup(shellLookupRequest);

		String shellId = null;
		SubModelResponse foundSubmodel = null;
		ReturnDataClass checkShellforSubmodelExistorNot = null;

		if (shellIds.isEmpty()) {
			// We don't need to create parent shell from aspect relationship
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					"No parent aspect found in DT: " + shellLookupRequest.toJsonString());
		} else {
			checkShellforSubmodelExistorNot = checkShellforSubmodelExistorNot(aspectRelationShip, shellLookupRequest,
					shellIds);
			foundSubmodel = checkShellforSubmodelExistorNot.getFoundSubmodel();
		}

		shellId = aspectRelationShip.getShellId();
		CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(aspectRelationShip);

		if (foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			digitalTwinfacilitaor.createSubModel(shellId, createSubModelRequest);
		} else {
			if (!foundSubmodel.getId().equals(createSubModelRequest.getId())) {
				digitalTwinfacilitaor.updateSubModel(shellId, foundSubmodel.getId(), createSubModelRequest);
				aspectRelationShip.setOldSubmodelIdforUpdateCase(foundSubmodel.getId());
			}
			aspectRelationShip.setUpdated(CommonConstants.UPDATED_Y);
			logDebug(String.format("Complete Digital Twins Update for '%s'", shellId));
		}
		aspectRelationShip.setSubModelId(createSubModelRequest.getId());
		aspectRelationShip.setChildUuid(createSubModelRequest.getId());

		return aspectRelationShip;
	}

	private ReturnDataClass checkShellforSubmodelExistorNot(AspectRelationship aspectRelationShip,
			ShellLookupRequest shellLookupRequest, List<String> shellIds) throws CsvHandlerDigitalTwinUseCaseException {

		List<ShellDescriptorResponse> items = digitalTwinfacilitaor.getShellDescriptorsWithSubmodelDetails(shellIds);

		List<String> submodelExistinceCount = new ArrayList<>();

		ReturnDataClass returnDataClass = new ReturnDataClass();

		for (ShellDescriptorResponse shellDescriptorResponse : items) {
			findMatchingSubmodel(aspectRelationShip, submodelExistinceCount, shellDescriptorResponse, returnDataClass);
		}

		if (returnDataClass.getFoundSubmodel() == null && shellIds.size() > 1)
			throw new CsvHandlerDigitalTwinUseCaseException(String
					.format("Multiple shell id's found for parent twin in DT %s", shellLookupRequest.toJsonString()));

		if (submodelExistinceCount.size() > 1)
			throw new CsvHandlerDigitalTwinUseCaseException(String.format(
					"%s submodel found multiple times in shells %s", getIdShortOfModel(), submodelExistinceCount));

		return returnDataClass;
	}

	private void findMatchingSubmodel(AspectRelationship aspectRelationShip, List<String> submodelExistinceCount,
			ShellDescriptorResponse shellDescriptorResponse, ReturnDataClass returnDataClass) {

		aspectRelationShip.setShellId(shellDescriptorResponse.getIdentification());
		aspectRelationShip.setParentUuid(shellDescriptorResponse.getGlobalAssetId());

		for (SubModelResponse subModelResponse : shellDescriptorResponse.getSubmodelDescriptors()) {

			if (subModelResponse != null && getIdShortOfModel().equals(subModelResponse.getIdShort())) {
				aspectRelationShip.setSubModelId(subModelResponse.getId());
				aspectRelationShip.setChildUuid(subModelResponse.getId());
				submodelExistinceCount.add(subModelResponse.getId());
				returnDataClass.setFoundShellDescriptorResponse(shellDescriptorResponse);
				returnDataClass.setFoundSubmodel(subModelResponse);
			}
		}
	}

	private Map<String, String> getSpecificAssetIds(AspectRelationship aspectRelationShip) {
		Map<String, String> specificIdentifiers = new HashMap<>();
		specificIdentifiers.put(CommonConstants.PART_INSTANCE_ID, aspectRelationShip.getParentPartInstanceId());
		specificIdentifiers.put(CommonConstants.MANUFACTURER_PART_ID, aspectRelationShip.getParentManufacturerPartId());
		specificIdentifiers.put(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId());
		if (aspectRelationShip.hasOptionalParentIdentifier()) {
			specificIdentifiers.put(aspectRelationShip.getParentOptionalIdentifierKey(),
					aspectRelationShip.getParentOptionalIdentifierValue());
		}

		return specificIdentifiers;
	}

	private Map<String, String> getShellLookupRequestforChild(AspectRelationship aspectRelationShip) {
		Map<String, String> shellLookupRequest = new HashMap<>();
		shellLookupRequest.put(CommonConstants.PART_INSTANCE_ID, aspectRelationShip.getChildPartInstanceId());
		shellLookupRequest.put(CommonConstants.MANUFACTURER_PART_ID, aspectRelationShip.getChildManufacturerPartId());
		shellLookupRequest.put(CommonConstants.MANUFACTURER_ID, aspectRelationShip.getChildManufacturerId());

		if (aspectRelationShip.hasOptionalChildIdentifier()) {
			shellLookupRequest.put(aspectRelationShip.getChildOptionalIdentifierKey(),
					aspectRelationShip.getChildOptionalIdentifierValue());
		}

		return shellLookupRequest;
	}

	@SneakyThrows
	private CreateSubModelRequest getCreateSubModelRequest(AspectRelationship aspectRelationShip) {

		ShellLookupRequest shellLookupRequest = digitalTwinsUtility
				.getShellLookupRequest(getShellLookupRequestforChild(aspectRelationShip));

		String childManufacturerId = aspectRelationShip.getChildManufacturerId();
		List<QueryDataOfferModel> queryDataOffers = edcAssetUrlCacheService.getDDTRUrl(childManufacturerId);

		String childUUID = null;
		String msg = "";

		for (QueryDataOfferModel dtOffer : queryDataOffers) {

			EDRCachedByIdResponse edrToken = edcAssetUrlCacheService.verifyAndGetToken(childManufacturerId, dtOffer);

			if (edrToken != null) {
				childUUID = lookUpChildTwin(shellLookupRequest, aspectRelationShip, edrToken, dtOffer);
				if (childUUID != null) {
					break;
				} 
			} else {
				msg = ", EDC connector " + dtOffer.getConnectorOfferUrl() + ", assetId " + dtOffer.getAssetId()
						+ ", The EDR token is null to find child twin ";
				log.warn(aspectRelationShip.getRowNumber() + msg + shellLookupRequest.toJsonString());
			}
		}

		if (queryDataOffers.isEmpty()) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					"No DTR registry found for child aspect look up");
		}

		if (childUUID == null) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					"No child aspect found for " + shellLookupRequest.toJsonString());
		}

		return digitalTwinsUtility.getCreateSubModelRequest(aspectRelationShip.getShellId(),
				getsemanticIdOfModel(), getIdShortOfModel(), childUUID, getNameOfModel(), aspectRelationShip.getParentUuid());

	}

	@SneakyThrows
	private String lookUpChildTwin(ShellLookupRequest shellLookupRequest, AspectRelationship aspectRelationShip,
			EDRCachedByIdResponse edrToken, QueryDataOfferModel dtOffer) {
		String childUUID = null;
		String endpoint = edrToken.getEndpoint();
		String dtOfferUrl = dtOffer.getConnectorOfferUrl();
		try {

			Map<String, String> header = new HashMap<>();
			header.put(edrToken.getAuthKey(), edrToken.getAuthCode());
			header.put("Edc-Bpn", aspectRelationShip.getChildManufacturerId());

			ShellLookupResponse shellLookup = eDCDigitalTwinProxyForLookUp.shellLookup(new URI(endpoint),
					digitalTwinsUtility.encodeAssetIdsObject(shellLookupRequest), header);

			childUUID = getChildSubmodelDetails(shellLookupRequest, endpoint, header, aspectRelationShip, dtOfferUrl,
					shellLookup.getResult());

		} catch (FeignException e) {
			String err = e.contentUTF8();
			err = StringUtils.isBlank(err) ? e.getMessage() : err;
			String errorMsg = "Unable to look up child twin " + dtOfferUrl + ", assetId " + dtOffer.getAssetId() + ", "
					+ shellLookupRequest.toJsonString() + " because: " + err;
			log.error("FeignException : " + errorMsg);
		} catch (CsvHandlerDigitalTwinUseCaseException e) {
			throw e;
		} catch (Exception e) {
			String errorMsg = "Unable to look up child twin " + dtOfferUrl + ", assetId " + dtOffer.getAssetId() + ", "
					+ shellLookupRequest.toJsonString() + "because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}

		return childUUID;
	}

	@SneakyThrows
	private String getChildSubmodelDetails(ShellLookupRequest shellLookupRequest, String endpoint,
			Map<String, String> header, AspectRelationship aspectRelationShip, String dtOfferUrl,
			List<String> childshellIds) throws CsvHandlerDigitalTwinUseCaseException {

		String childUUID = null;
		if (childshellIds == null) {
			log.warn(aspectRelationShip.getRowNumber() + ", " + dtOfferUrl + ", No child aspect found for "
					+ shellLookupRequest.toJsonString());
		} else if (childshellIds.size() > 1) {
			throw new CsvHandlerDigitalTwinUseCaseException(String.format(
					"Multiple shell id's found for childAspect %s, %s", dtOfferUrl, shellLookupRequest.toJsonString()));
		} else if (childshellIds.size() == 1) {

			ShellDescriptorResponse shellDescriptorResponse = eDCDigitalTwinProxyForLookUp.getShellDescriptorByShellId(
					new URI(endpoint), encodeShellIdBase64Utf8(childshellIds.get(0)), header);
			childUUID = shellDescriptorResponse.getGlobalAssetId();

			log.info(aspectRelationShip.getRowNumber() + ", " + dtOfferUrl + ", Child aspect found for "
					+ shellLookupRequest.toJsonString());
		}

		return childUUID;
	}

	private String encodeShellIdBase64Utf8(String shellId) {
		return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
	}

	@Data
	private class ReturnDataClass {
		private SubModelResponse foundSubmodel;
		private ShellDescriptorResponse foundShellDescriptorResponse;
	}
}