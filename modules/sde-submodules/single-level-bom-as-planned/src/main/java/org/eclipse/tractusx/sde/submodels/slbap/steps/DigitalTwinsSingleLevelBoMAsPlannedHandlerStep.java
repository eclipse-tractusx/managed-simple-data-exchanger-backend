/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.submodels.slbap.steps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.mapper.PartAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlanned;
import org.eclipse.tractusx.sde.submodels.pap.repository.PartAsPlannedRepository;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlanned;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class DigitalTwinsSingleLevelBoMAsPlannedHandlerStep extends Step {

	private final DigitalTwinsFacilitator digitalTwinsFacilitator;
	private final PartAsPlannedRepository partAsPlannedRepository;
	private final PartAsPlannedMapper partAsPlannedMapper;
	private final DigitalTwinsUtility digitalTwinsUtility;

	@SneakyThrows
	public SingleLevelBoMAsPlanned run(SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect, PolicyModel policy)
			throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(singleLevelBoMAsPlannedAspect, policy);
		} catch (Exception e) {
			throw new ServiceException(
					singleLevelBoMAsPlannedAspect.getRowNumber() + ": DigitalTwins: " + e.getMessage());
		}
	}

	@SneakyThrows
	private SingleLevelBoMAsPlanned doRun(SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect, PolicyModel policy)
			throws CsvHandlerUseCaseException, CsvHandlerDigitalTwinUseCaseException {

		ShellLookupRequest shellLookupRequest = getShellLookupRequest(singleLevelBoMAsPlannedAspect);
		List<String> shellIds = digitalTwinsFacilitator.shellLookup(shellLookupRequest);

		String shellId;
		ShellDescriptorRequest aasDescriptorRequest = lookUpParentEntityLocaly(singleLevelBoMAsPlannedAspect, policy);

		if (shellIds.isEmpty()) {

			logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
			ShellDescriptorResponse result = digitalTwinsFacilitator.createShellDescriptor(aasDescriptorRequest);
			shellId = result.getIdentification();
			logDebug(String.format("Shell created with id '%s'", shellId));

		} else if (shellIds.size() == 1) {

			logDebug(String.format("Shell id found for '%s'", shellLookupRequest.toJsonString()));
			shellId = shellIds.stream().findFirst().orElse(null);

			logDebug(String.format("Shell id '%s'", shellId));
		} else {
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple id's found on childAspect %s", shellLookupRequest.toJsonString()));
		}

		singleLevelBoMAsPlannedAspect.setShellId(shellId);
		SubModelListResponse subModelResponse = digitalTwinsFacilitator.getSubModels(shellId);
		SubModelResponse foundSubmodel = null;
		if (subModelResponse != null) {
			foundSubmodel = subModelResponse.getResult().stream()
					.filter(x -> getIdShortOfModel().equals(x.getIdShort())).findFirst().orElse(null);
			if (foundSubmodel != null)
				singleLevelBoMAsPlannedAspect.setSubModelId(foundSubmodel.getId());
		}

		if (subModelResponse == null || foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));

			CreateSubModelRequest createSubModelRequest = digitalTwinsUtility.getCreateSubModelRequest(
					singleLevelBoMAsPlannedAspect.getShellId(), getsemanticIdOfModel(), getIdShortOfModel(),
					getNameOfModel(), singleLevelBoMAsPlannedAspect.getParentUuid(), null);

			digitalTwinsFacilitator.updateShellDetails(shellId, aasDescriptorRequest, createSubModelRequest);
			singleLevelBoMAsPlannedAspect.setSubModelId(createSubModelRequest.getId());
		} else {
			// There is no need to send submodel because of nothing to change in it so sending null of it
			digitalTwinsFacilitator.updateShellDetails(shellId, aasDescriptorRequest, null);
			singleLevelBoMAsPlannedAspect.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}
		return singleLevelBoMAsPlannedAspect;
	}

	private ShellDescriptorRequest lookUpParentEntityLocaly(SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect,
			PolicyModel policy) throws CsvHandlerUseCaseException {

		PartAsPlannedEntity partAsPlannedEntity = null;
		partAsPlannedEntity = partAsPlannedRepository
				.findByIdentifiers(singleLevelBoMAsPlannedAspect.getParentManufacturerPartId());

		if (partAsPlannedEntity == null) {
			throw new CsvHandlerUseCaseException(singleLevelBoMAsPlannedAspect.getRowNumber(),
					"No parent aspect found");
		}

		return digitalTwinsUtility.getShellDescriptorRequest(partAsPlannedEntity.getNameAtManufacturer(),
				partAsPlannedEntity.getManufacturerPartId(), partAsPlannedEntity.getUuid(),
				getSpecificAssetIds(partAsPlannedMapper.mapFrom(partAsPlannedEntity)), policy);
	}

	private Map<String, String> getSpecificAssetIds(PartAsPlanned partAsPlannedAspect) {
		Map<String, String> specificIdentifiers = new HashMap<>();
		specificIdentifiers.put(CommonConstants.MANUFACTURER_PART_ID, partAsPlannedAspect.getManufacturerPartId());
		specificIdentifiers.put(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId());
		specificIdentifiers.put(CommonConstants.ASSET_LIFECYCLE_PHASE, CommonConstants.AS_PLANNED);

		return specificIdentifiers;
	}

	private ShellLookupRequest getShellLookupRequest(SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect) {

		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		getSpecificAssetIdsForSingleLevel(singleLevelBoMAsPlannedAspect).entrySet().stream()
				.forEach(entry -> shellLookupRequest.addLocalIdentifier(entry.getKey(), entry.getValue()));

		return shellLookupRequest;
	}

	private Map<String, String> getSpecificAssetIdsForSingleLevel(
			SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect) {
		Map<String, String> specificIdentifiers = new HashMap<>();
		specificIdentifiers.put(CommonConstants.ASSET_LIFECYCLE_PHASE, CommonConstants.AS_PLANNED);
		specificIdentifiers.put(CommonConstants.MANUFACTURER_PART_ID,
				singleLevelBoMAsPlannedAspect.getParentManufacturerPartId());
		specificIdentifiers.put(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId());
		return specificIdentifiers;
	}

}