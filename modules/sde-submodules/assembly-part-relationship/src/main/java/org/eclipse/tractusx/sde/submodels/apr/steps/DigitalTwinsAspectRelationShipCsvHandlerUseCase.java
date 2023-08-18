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

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.springframework.stereotype.Service;

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
		List<ShellDescriptorResponse> items = digitalTwinfacilitaor.getShellDescriptorsWithSubmodelDetails(shellIds, null);

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

		List<String> dtURls = getDDTRUrl(aspectRelationShip.getChildManufacturerId());

		String childUUID = null;

		for (String ddtUrl : dtURls) {

			List<String> childshellIds = digitalTwinfacilitaor.shellLookupFromDDTR(shellLookupRequest, ddtUrl);

			if (childshellIds.isEmpty()) {
				log.warn(aspectRelationShip.getRowNumber() + ", " + ddtUrl + ", No child aspect found for "
						+ shellLookupRequest.toJsonString());
			}

			if (childshellIds.size() > 1) {
				log.warn(String.format("Multiple shell id's found for childAspect %s, %s", ddtUrl,
						shellLookupRequest.toJsonString()));
			}

			if (childshellIds.size() == 1) {
				ShellDescriptorResponse shellDescriptorResponse = digitalTwinfacilitaor
						.getShellDetailsById(childshellIds.get(0), ddtUrl);
				childUUID = shellDescriptorResponse.getGlobalAssetId();
			}

		}

		if (dtURls.isEmpty()) {
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

	public List<String> getDDTRUrl(String bpnNumber) {

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

}