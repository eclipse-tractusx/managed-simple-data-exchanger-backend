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

package org.eclipse.tractusx.sde.submodels.spt.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.Endpoint;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.GlobalAssetId;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.KeyValuePair;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.SemanticId;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.DigitalTwinGateway;
import org.eclipse.tractusx.sde.submodels.spt.model.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

@Service
public class DigitalTwinsAspectCsvHandlerUseCase extends Step {

	
	@Autowired
	private DigitalTwinGateway gateway;
	
	@Autowired
	private DigitalTwinsUtility digitalTwinsUtility;

	@SneakyThrows
	public Aspect run(Aspect aspect) throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(aspect);
		} catch (Exception e) {
			throw new ServiceException(aspect.getRowNumber() + ": DigitalTwins: " + e.getMessage());
		}
	}

	@SneakyThrows
	private Aspect doRun(Aspect aspect) throws CsvHandlerDigitalTwinUseCaseException {
		ShellLookupRequest shellLookupRequest = getShellLookupRequest(aspect);
		ShellLookupResponse shellIds = gateway.shellLookup(shellLookupRequest);

		String shellId;

		if (shellIds.isEmpty()) {
			logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
			ShellDescriptorRequest aasDescriptorRequest = getShellDescriptorRequest(aspect);
			ShellDescriptorResponse result = gateway.createShellDescriptor(aasDescriptorRequest);
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

		aspect.setShellId(shellId);
		SubModelListResponse subModelResponse = gateway.getSubModels(shellId);
		SubModelResponse foundSubmodel = null;
		if (subModelResponse != null) {
			foundSubmodel = subModelResponse.stream().filter(x -> getIdShortOfModel().equals(x.getIdShort()))
					.findFirst().orElse(null);
			if (foundSubmodel != null)
				aspect.setSubModelId(foundSubmodel.getIdentification());
		}

		if (subModelResponse == null || foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(aspect);
			gateway.createSubModel(shellId, createSubModelRequest);
			aspect.setSubModelId(createSubModelRequest.getIdentification());
		} else {
			aspect.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}

		return aspect;
	}

	private ShellLookupRequest getShellLookupRequest(Aspect aspect) {
		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(CommonConstants.PART_INSTANCE_ID, aspect.getPartInstanceId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_PART_ID, aspect.getManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId());
		if (aspect.hasOptionalIdentifier()) {
			shellLookupRequest.addLocalIdentifier(aspect.getOptionalIdentifierKey(),
					aspect.getOptionalIdentifierValue());
		}
		return shellLookupRequest;
	}

	private CreateSubModelRequest getCreateSubModelRequest(Aspect aspect) {
		ArrayList<String> value = new ArrayList<>();
		value.add(getsemanticIdOfModel());
		SemanticId semanticId = new SemanticId(value);
		String identification = CommonConstants.PREFIX + UUID.randomUUID();

		List<Endpoint> endpoints = digitalTwinsUtility.prepareDtEndpoint(aspect.getShellId(), identification);

		return CreateSubModelRequest.builder().idShort(getIdShortOfModel()).identification(identification).semanticId(semanticId)
				.endpoints(endpoints).build();
	}

	private ShellDescriptorRequest getShellDescriptorRequest(Aspect aspect) {
		ArrayList<KeyValuePair> specificIdentifiers = new ArrayList<>();
		setSpecifiers(specificIdentifiers, aspect);

		List<String> values = new ArrayList<>();
		values.add(aspect.getUuid());
		GlobalAssetId globalIdentifier = new GlobalAssetId(values);

		return ShellDescriptorRequest.builder()
				.idShort(String.format("%s_%s_%s", aspect.getNameAtManufacturer(), digitalTwinsUtility.getManufacturerId(),
						aspect.getManufacturerPartId()))
				.globalAssetId(globalIdentifier).specificAssetIds(specificIdentifiers)
				.identification(CommonConstants.PREFIX + UUID.randomUUID()).build();
	}

	private void setSpecifiers(final ArrayList<KeyValuePair> specificIdentifiers, Aspect aspect) {
		specificIdentifiers.add(new KeyValuePair(CommonConstants.PART_INSTANCE_ID, aspect.getPartInstanceId()));
		specificIdentifiers.add(new KeyValuePair(CommonConstants.MANUFACTURER_PART_ID, aspect.getManufacturerPartId()));
		specificIdentifiers.add(new KeyValuePair(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId()));
		if (aspect.hasOptionalIdentifier()) {
			specificIdentifiers
					.add(new KeyValuePair(aspect.getOptionalIdentifierKey(), aspect.getOptionalIdentifierValue()));
		}
	}

}