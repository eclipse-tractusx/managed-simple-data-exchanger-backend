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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
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
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalTwinsAspectRelationShipCsvHandlerUseCase extends Step {

	private final DigitalTwinsFacilitator digitalTwinfacilitaor;
	private final DigitalTwinsUtility digitalTwinsUtility;
	private final DDTRUrlCacheUtility dDTRUrlCacheUtility;
	private final EDCDigitalTwinProxyForLookUp eDCDigitalTwinProxyForLookUp;

	private static final Map<String, LocalDateTime> map = new ConcurrentHashMap<>();

	@SneakyThrows
	public AspectRelationship run(AspectRelationship aspectRelationShip) throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(aspectRelationShip);
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					": DigitalTwins: " + e.getMessage());
		}
	}

	@SneakyThrows
	private AspectRelationship doRun(AspectRelationship aspectRelationShip)
			throws CsvHandlerUseCaseException, CsvHandlerDigitalTwinUseCaseException {

		ShellLookupRequest shellLookupRequest = getShellLookupRequest(aspectRelationShip);
		List<String> shellIds = digitalTwinfacilitaor.shellLookup(shellLookupRequest);

		String shellId = null;
		SubModelResponse foundSubmodel = null;

		if (shellIds.isEmpty()) {
			// We don't need to create parent shell from aspect relationship
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					"No parent aspect found in DT: " + shellLookupRequest.toJsonString());
		} else {
			foundSubmodel = checkShellforSubmodelExistorNot(aspectRelationShip, shellLookupRequest, shellIds,
					foundSubmodel);
		}

		shellId = aspectRelationShip.getShellId();
		CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(aspectRelationShip);

		if (foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			createSubModelSteps(aspectRelationShip, shellId, createSubModelRequest);
		} else {
			if (!foundSubmodel.getId().equals(createSubModelRequest.getId())) {
				digitalTwinfacilitaor.deleteSubmodelfromShellById(shellId, foundSubmodel.getId());
				createSubModelSteps(aspectRelationShip, shellId, createSubModelRequest);
				aspectRelationShip.setOldSubmodelIdforUpdateCase(foundSubmodel.getId());
			}
			aspectRelationShip.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}

		return aspectRelationShip;
	}

	private SubModelResponse checkShellforSubmodelExistorNot(AspectRelationship aspectRelationShip,
			ShellLookupRequest shellLookupRequest, List<String> shellIds, SubModelResponse foundSubmodel)
			throws CsvHandlerDigitalTwinUseCaseException {
		List<ShellDescriptorResponse> items = digitalTwinfacilitaor.getShellDescriptorsWithSubmodelDetails(shellIds);

		List<String> submodelExistinceCount = new ArrayList<>();

		for (ShellDescriptorResponse shellDescriptorResponse : items) {

			foundSubmodel = findMatchingSubmodel(aspectRelationShip, foundSubmodel, submodelExistinceCount,
					shellDescriptorResponse);
		}

		if (foundSubmodel == null && shellIds.size() > 1)
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple shell id's found for parent in DT %s", shellLookupRequest.toJsonString()));

		if (submodelExistinceCount.size() > 1)
			throw new CsvHandlerDigitalTwinUseCaseException(String.format(
					"%s submodel found multiple times in shells %s", getIdShortOfModel(), submodelExistinceCount));
		return foundSubmodel;
	}

	private SubModelResponse findMatchingSubmodel(AspectRelationship aspectRelationShip, SubModelResponse foundSubmodel,
			List<String> submodelExistinceCount, ShellDescriptorResponse shellDescriptorResponse) {
		aspectRelationShip.setShellId(shellDescriptorResponse.getIdentification());
		aspectRelationShip.setParentUuid(shellDescriptorResponse.getGlobalAssetId());

		for (SubModelResponse subModelResponse : shellDescriptorResponse.getSubmodelDescriptors()) {

			if (subModelResponse != null && getIdShortOfModel().equals(subModelResponse.getIdShort())) {
				aspectRelationShip.setSubModelId(subModelResponse.getId());
				aspectRelationShip.setChildUuid(subModelResponse.getId());
				foundSubmodel = subModelResponse;
				submodelExistinceCount.add(aspectRelationShip.getShellId());
			}
		}
		return foundSubmodel;
	}

	private void createSubModelSteps(AspectRelationship aspectRelationShip, String shellId,
			CreateSubModelRequest createSubModelRequest) {
		digitalTwinfacilitaor.createSubModel(shellId, createSubModelRequest);
		aspectRelationShip.setSubModelId(createSubModelRequest.getId());
		aspectRelationShip.setChildUuid(createSubModelRequest.getId());
	}

	private ShellLookupRequest getShellLookupRequest(AspectRelationship aspectRelationShip) {
		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(CommonConstants.PART_INSTANCE_ID,
				aspectRelationShip.getParentPartInstanceId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_PART_ID,
				aspectRelationShip.getParentManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId());

		if (aspectRelationShip.hasOptionalParentIdentifier()) {
			shellLookupRequest.addLocalIdentifier(aspectRelationShip.getParentOptionalIdentifierKey(),
					aspectRelationShip.getParentOptionalIdentifierValue());
		}

		return shellLookupRequest;
	}

	private ShellLookupRequest getShellLookupRequestforChild(AspectRelationship aspectRelationShip) {
		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(CommonConstants.PART_INSTANCE_ID,
				aspectRelationShip.getChildPartInstanceId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_PART_ID,
				aspectRelationShip.getChildManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_ID,
				aspectRelationShip.getChildManufacturerId());

		if (aspectRelationShip.hasOptionalChildIdentifier()) {
			shellLookupRequest.addLocalIdentifier(aspectRelationShip.getChildOptionalIdentifierKey(),
					aspectRelationShip.getChildOptionalIdentifierValue());
		}

		return shellLookupRequest;
	}

	@SneakyThrows
	private CreateSubModelRequest getCreateSubModelRequest(AspectRelationship aspectRelationShip) {

		ShellLookupRequest shellLookupRequest = getShellLookupRequestforChild(aspectRelationShip);

		String childManufacturerId = aspectRelationShip.getChildManufacturerId();
		List<QueryDataOfferModel> queryDataOffers = getDDTRUrl(childManufacturerId);

		String childUUID = null;
		String msg = "";

		for (QueryDataOfferModel dtOffer : queryDataOffers) {

			EDRCachedByIdResponse edrToken = dDTRUrlCacheUtility.verifyAndGetToken(childManufacturerId, dtOffer);

			if (edrToken != null) {
				childUUID = lookUpChildTwin(shellLookupRequest, aspectRelationShip, edrToken, dtOffer);
				if (childUUID != null) {
					break;
				} else {
					log.warn(aspectRelationShip.getRowNumber() + ", EDC connector " + dtOffer.getConnectorOfferUrl()
							+ ", No child twin found for " + shellLookupRequest.toJsonString());
				}
			} else {
				msg = ", EDC connector " + dtOffer.getConnectorOfferUrl()
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

		return digitalTwinsUtility.getCreateSubModelRequestForChild(aspectRelationShip.getShellId(),
				getsemanticIdOfModel(), getIdShortOfModel(), childUUID);

	}

	@SneakyThrows
	private String lookUpChildTwin(ShellLookupRequest shellLookupRequest, AspectRelationship aspectRelationShip,
			EDRCachedByIdResponse edrToken, QueryDataOfferModel dtOffer) {
		String childUUID = null;
		String endpoint = edrToken.getEndpoint();
		String dtOfferUrl = dtOffer.getConnectorOfferUrl();
		try {

			Map<String, String> header = Map.of(edrToken.getAuthKey(), edrToken.getAuthCode());

			ResponseEntity<ShellLookupResponse> shellLookup = eDCDigitalTwinProxyForLookUp
					.shellLookup(new URI(endpoint), shellLookupRequest.toJsonString(), header);
			ShellLookupResponse body = shellLookup.getBody();

			if (shellLookup.getStatusCode() == HttpStatus.OK && body != null) {
				childUUID = getChildSubmodelDetails(shellLookupRequest, endpoint, header, aspectRelationShip,
						dtOfferUrl, body.getResult());
			}

		} catch (FeignException e) {
			String errorMsg = "Unable to look up child twin " + dtOfferUrl + ", " + shellLookupRequest.toJsonString()
					+ " because: " + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to look up child twin " + dtOfferUrl + ", " + shellLookupRequest.toJsonString()
					+ "because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}

		return childUUID;
	}

	@SneakyThrows
	private String getChildSubmodelDetails(ShellLookupRequest shellLookupRequest, String endpoint,
			Map<String, String> header, AspectRelationship aspectRelationShip, String dtOfferUrl,
			List<String> childshellIds) {

		String childUUID = null;
		if (childshellIds == null) {
			log.warn(aspectRelationShip.getRowNumber() + ", " + dtOfferUrl + ", No child aspect found for "
					+ shellLookupRequest.toJsonString());
		} else if (childshellIds.size() > 1) {
			log.warn(String.format("Multiple shell id's found for childAspect %s, %s", dtOfferUrl,
					shellLookupRequest.toJsonString()));
		} else if (childshellIds.size() == 1) {
			ResponseEntity<ShellDescriptorResponse> shellDescriptorResponse = eDCDigitalTwinProxyForLookUp
					.getShellDescriptorByShellId(new URI(endpoint), encodeShellIdBase64Utf8(childshellIds.get(0)),
							header);
			ShellDescriptorResponse shellDescriptorResponseBody = shellDescriptorResponse.getBody();
			if (shellDescriptorResponse.getStatusCode() == HttpStatus.OK && shellDescriptorResponseBody != null) {
				childUUID = shellDescriptorResponseBody.getGlobalAssetId();
				log.debug(aspectRelationShip.getRowNumber() + ", " + dtOfferUrl + ", Child aspect found for "
						+ shellLookupRequest.toJsonString());
			}
		}

		return childUUID;
	}

	public List<QueryDataOfferModel> getDDTRUrl(String bpnNumber) {

		LocalDateTime cacheExpTime = map.get(bpnNumber);
		LocalDateTime currDate = LocalDateTime.now();

		if (cacheExpTime == null)
			cacheExpTime = currDate.plusHours(12);
		else if (currDate.isAfter(cacheExpTime)) {
			dDTRUrlCacheUtility.removeDDTRUrlCache(bpnNumber);
			cacheExpTime = currDate.plusHours(12);
		}
		map.put(bpnNumber, cacheExpTime);
		return dDTRUrlCacheUtility.getDDTRUrl(bpnNumber);
	}

	public void clearDDTRUrlCache() {
		map.clear();
		dDTRUrlCacheUtility.cleareDDTRUrlAllCache();
	}

	private String encodeShellIdBase64Utf8(String shellId) {
		return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
	}
}