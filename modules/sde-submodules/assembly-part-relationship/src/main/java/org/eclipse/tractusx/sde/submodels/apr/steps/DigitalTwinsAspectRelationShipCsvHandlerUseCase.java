/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.Endpoint;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.GlobalAssetId;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.KeyValuePair;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.SemanticId;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubmodelDescriptionListResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.DigitalTwinGateway;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.mapper.AspectMapper;
import org.eclipse.tractusx.sde.submodels.spt.model.Aspect;
import org.eclipse.tractusx.sde.submodels.spt.repository.AspectRepository;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

@Service
public class DigitalTwinsAspectRelationShipCsvHandlerUseCase extends Step {

	private final DigitalTwinGateway gateway;
	private final AspectRepository aspectRepository;
	private final AspectMapper aspectMapper;
	private final DigitalTwinsUtility digitalTwinsUtility;

	public DigitalTwinsAspectRelationShipCsvHandlerUseCase(DigitalTwinGateway gateway,
			AspectRepository aspectRepository, AspectMapper aspectMapper,DigitalTwinsUtility digitalTwinsUtility) {
		this.gateway = gateway;
		this.aspectRepository = aspectRepository;
		this.aspectMapper = aspectMapper;
		this.digitalTwinsUtility = digitalTwinsUtility; 
	}

	@SneakyThrows
	public AspectRelationship run(AspectRelationship aspectRelationShip) throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(aspectRelationShip);
		} catch (Exception e) {
			throw new ServiceException(aspectRelationShip.getRowNumber() + ": DigitalTwins: " + e.getMessage());
		}
	}

	@SneakyThrows
	private AspectRelationship doRun(AspectRelationship aspectRelationShip)
			throws CsvHandlerUseCaseException, CsvHandlerDigitalTwinUseCaseException {

		ShellLookupRequest shellLookupRequest = getShellLookupRequest(aspectRelationShip);
		ShellLookupResponse shellIds = gateway.shellLookup(shellLookupRequest);

		String shellId = null;
		SubModelResponse foundSubmodel = null;

		if (shellIds.isEmpty()) {
			ShellDescriptorResponse shellDescriptorResponse = createShellDescriptor(aspectRelationShip,
					shellLookupRequest);
			aspectRelationShip.setParentUuid(shellDescriptorResponse.getGlobalAssetId().getValue().get(0));
			aspectRelationShip.setShellId(shellDescriptorResponse.getIdentification());

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
			if (!foundSubmodel.getIdentification().equals(createSubModelRequest.getIdentification())) {
				gateway.deleteSubmodelfromShellById(shellId, foundSubmodel.getIdentification());
				createSubModelSteps(aspectRelationShip, shellId, createSubModelRequest);
			}
			aspectRelationShip.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}

		return aspectRelationShip;
	}

	private SubModelResponse checkShellforSubmodelExistorNot(AspectRelationship aspectRelationShip,
			ShellLookupRequest shellLookupRequest, ShellLookupResponse shellIds, SubModelResponse foundSubmodel)
			throws CsvHandlerDigitalTwinUseCaseException {
		SubmodelDescriptionListResponse shellDescriptorWithsubmodelDetails = gateway
				.getShellDescriptorsWithSubmodelDetails(shellIds);

		List<String> submodelExistinceCount = new ArrayList<>();

		for (ShellDescriptorResponse shellDescriptorResponse : shellDescriptorWithsubmodelDetails.getItems()) {

			foundSubmodel = findMatchingSubmodel(aspectRelationShip, foundSubmodel, submodelExistinceCount,
					shellDescriptorResponse);
		}

		if (foundSubmodel == null && shellIds.size() > 1)
			throw new CsvHandlerDigitalTwinUseCaseException(String
					.format("Multiple shell id's found on childAspect %s", shellLookupRequest.toJsonString()));

		if (submodelExistinceCount.size() > 1)
			throw new CsvHandlerDigitalTwinUseCaseException(String.format(
					"%s submodel found multiple times in shells %s", getIdShortOfModel(), submodelExistinceCount));
		return foundSubmodel;
	}

	private SubModelResponse findMatchingSubmodel(AspectRelationship aspectRelationShip, SubModelResponse foundSubmodel,
			List<String> submodelExistinceCount, ShellDescriptorResponse shellDescriptorResponse) {
		aspectRelationShip.setShellId(shellDescriptorResponse.getIdentification());
		aspectRelationShip.setParentUuid(shellDescriptorResponse.getGlobalAssetId().getValue().get(0));

		for (SubModelResponse subModelResponse : shellDescriptorResponse.getSubmodelDescriptors()) {

			if (subModelResponse != null && getIdShortOfModel().equals(subModelResponse.getIdShort())) {
				aspectRelationShip.setSubModelId(subModelResponse.getIdentification());
				aspectRelationShip.setChildUuid(subModelResponse.getIdentification());
				foundSubmodel = subModelResponse;
				submodelExistinceCount.add(aspectRelationShip.getShellId());
			}
		}
		return foundSubmodel;
	}

	private void createSubModelSteps(AspectRelationship aspectRelationShip, String shellId,
			CreateSubModelRequest createSubModelRequest) {
		gateway.createSubModel(shellId, createSubModelRequest);
		aspectRelationShip.setSubModelId(createSubModelRequest.getIdentification());
		aspectRelationShip.setChildUuid(createSubModelRequest.getIdentification());
	}

	private ShellDescriptorResponse createShellDescriptor(AspectRelationship aspectRelationShip,
			ShellLookupRequest shellLookupRequest) throws CsvHandlerUseCaseException {

		logInfo(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
		AspectEntity aspectEntity = null;
		if (aspectRelationShip.hasOptionalParentIdentifier()) {
			aspectEntity = aspectRepository.findByIdentifiers(aspectRelationShip.getParentPartInstanceId(),
					aspectRelationShip.getParentManufacturerPartId(),
					aspectRelationShip.getParentOptionalIdentifierKey(),
					aspectRelationShip.getParentOptionalIdentifierValue());
		} else {
			aspectEntity = aspectRepository.findByIdentifiers(aspectRelationShip.getParentPartInstanceId(),
					aspectRelationShip.getParentManufacturerPartId());
		}

		if (aspectEntity == null) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(), "No parent aspect found in DT as well as in Local SDE");
		}

		ShellDescriptorRequest aasDescriptorRequest = getShellDescriptorRequest(aspectMapper.mapFrom(aspectEntity));
		ShellDescriptorResponse result = gateway.createShellDescriptor(aasDescriptorRequest);
		logInfo(String.format("Shell created with id '%s'", result.getIdentification()));

		return result;
	}

	private ShellLookupRequest getShellLookupRequest(AspectRelationship aspectRelationShip) {
		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(CommonConstants.PART_INSTANCE_ID,
				aspectRelationShip.getParentPartInstanceId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_PART_ID,
				aspectRelationShip.getParentManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_ID,
				digitalTwinsUtility.getManufacturerId());

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

		if (aspectRelationShip.hasOptionalParentIdentifier()) {
			shellLookupRequest.addLocalIdentifier(aspectRelationShip.getChildOptionalIdentifierKey(),
					aspectRelationShip.getChildOptionalIdentifierValue());
		}

		return shellLookupRequest;
	}

	@SneakyThrows
	private CreateSubModelRequest getCreateSubModelRequest(AspectRelationship aspectRelationShip) {
		ArrayList<String> value = new ArrayList<>();
		value.add(getsemanticIdOfModel());

		ShellLookupRequest shellLookupRequest = getShellLookupRequestforChild(aspectRelationShip);
		ShellLookupResponse childshellIds = gateway.shellLookup(shellLookupRequest);

		String childUUID = null;

		if (childshellIds.isEmpty()) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(),
					"No child aspect found for " + shellLookupRequest.toJsonString());
		}

		if (childshellIds.size() > 1) {
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple shell id's found on childAspect %s", shellLookupRequest.toJsonString()));
		}

		SubmodelDescriptionListResponse shellDescriptorWithsubmodelDetails = gateway
				.getShellDescriptorsWithSubmodelDetails(childshellIds);

		for (ShellDescriptorResponse shellDescriptorResponse : shellDescriptorWithsubmodelDetails.getItems()) {
			childUUID = shellDescriptorResponse.getGlobalAssetId().getValue().get(0);
		}

		String identification = childUUID;
		SemanticId semanticId = new SemanticId(value);

		List<Endpoint> endpoints = digitalTwinsUtility.prepareDtEndpoint(aspectRelationShip.getShellId(), identification);

		return CreateSubModelRequest.builder().idShort(getIdShortOfModel()).identification(identification)
				.semanticId(semanticId).endpoints(endpoints).build();
	}

	private ShellDescriptorRequest getShellDescriptorRequest(Aspect aspect) {
		ArrayList<KeyValuePair> specificIdentifiers = new ArrayList<>();
		setSpecifiers(specificIdentifiers, aspect);
		List<String> values = new ArrayList<>();
		values.add(aspect.getUuid());
		GlobalAssetId globalIdentifier = new GlobalAssetId(values);

		return ShellDescriptorRequest.builder()
				.idShort(String.format("%s_%s_%s", aspect.getNameAtManufacturer(),
						digitalTwinsUtility.getManufacturerId(), aspect.getManufacturerPartId()))
				.globalAssetId(globalIdentifier).specificAssetIds(specificIdentifiers)
				.identification(UUIdGenerator.getUrnUuid()).build();
	}

	private void setSpecifiers(final ArrayList<KeyValuePair> specificIdentifiers, Aspect aspect) {
		specificIdentifiers.add(new KeyValuePair(CommonConstants.PART_INSTANCE_ID, aspect.getPartInstanceId()));
		specificIdentifiers.add(new KeyValuePair(CommonConstants.MANUFACTURER_PART_ID, aspect.getManufacturerPartId()));
		specificIdentifiers.add(
				new KeyValuePair(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId()));
		if (aspect.hasOptionalIdentifier()) {
			specificIdentifiers
					.add(new KeyValuePair(aspect.getOptionalIdentifierKey(), aspect.getOptionalIdentifierValue()));
		}
	}

}