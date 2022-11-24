/********************************************************************************
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
package org.eclipse.tractusx.sde.submodels.slbap.steps;

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
import org.eclipse.tractusx.sde.digitaltwins.entities.common.ProtocolInformation;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.SemanticId;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.DigitalTwinGateway;
import org.eclipse.tractusx.sde.submodels.pap.constants.PartAsPlannedConstants;
import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.mapper.PartAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlanned;
import org.eclipse.tractusx.sde.submodels.pap.repository.PartAsPlannedRepository;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlanned;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

@Service
public class DigitalTwinsSingleLevelBoMAsPlannedHandlerStep extends Step {
	
	@Autowired
	private PartAsPlannedConstants partAsPlannedConstants;
	
	private final DigitalTwinGateway gateway;
	private final PartAsPlannedRepository partAsPlannedRepository;
	private final PartAsPlannedMapper partAsPlannedMapper;


	public DigitalTwinsSingleLevelBoMAsPlannedHandlerStep(DigitalTwinGateway gateway,
			PartAsPlannedRepository partAsPlannedRepository, PartAsPlannedMapper partAsPlannedMapper) {
		this.gateway = gateway;
		this.partAsPlannedRepository = partAsPlannedRepository;
		this.partAsPlannedMapper = partAsPlannedMapper;
	}

	@SneakyThrows
	public SingleLevelBoMAsPlanned run(SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect) throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(singleLevelBoMAsPlannedAspect);
		} catch (Exception e) {
			throw new ServiceException(singleLevelBoMAsPlannedAspect.getRowNumber() + ": DigitalTwins: " + e.getMessage());
		}
	}

	private SingleLevelBoMAsPlanned doRun(SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect)
			throws CsvHandlerUseCaseException, CsvHandlerDigitalTwinUseCaseException {
		
		ShellLookupRequest shellLookupRequest = getShellLookupRequest(singleLevelBoMAsPlannedAspect);
		ShellLookupResponse shellIds = gateway.shellLookup(shellLookupRequest);

		String shellId;

		if (shellIds.isEmpty()) {
			shellId = createShellDescriptor(singleLevelBoMAsPlannedAspect, shellLookupRequest);
		} else if (shellIds.size() == 1) {
			logDebug(String.format("Shell id found for '%s'", shellLookupRequest.toJsonString()));
			shellId = shellIds.stream().findFirst().orElse(null);
			logDebug(String.format("Shell id '%s'", shellId));
		} else {
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple id's found on childAspect %s", shellLookupRequest.toJsonString()));
		}

		singleLevelBoMAsPlannedAspect.setShellId(shellId);
		SubModelListResponse subModelResponse = gateway.getSubModels(shellId);
		SubModelResponse foundSubmodel = null;
		if (subModelResponse != null) {
			foundSubmodel = subModelResponse.stream().filter(x -> getIdShortOfModel().equals(x.getIdShort()))
					.findFirst().orElse(null);
			if (foundSubmodel != null)
				singleLevelBoMAsPlannedAspect.setSubModelId(foundSubmodel.getIdentification());
		}

		if (subModelResponse == null || foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(singleLevelBoMAsPlannedAspect);
			gateway.createSubModel(shellId, createSubModelRequest);
			singleLevelBoMAsPlannedAspect.setSubModelId(createSubModelRequest.getIdentification());
		} else {
			singleLevelBoMAsPlannedAspect.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}
		return singleLevelBoMAsPlannedAspect;
	}

	private String createShellDescriptor(SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect, ShellLookupRequest shellLookupRequest)
			throws CsvHandlerUseCaseException {
		
		String shellId;
		logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
		
		PartAsPlannedEntity partAsPlannedEntity = null;
		partAsPlannedEntity = partAsPlannedRepository.findByIdentifiers(singleLevelBoMAsPlannedAspect.getParentManufacturerPartId());

		if (partAsPlannedEntity == null) {
			throw new CsvHandlerUseCaseException(singleLevelBoMAsPlannedAspect.getRowNumber(), "No parent aspect found");
		}

		ShellDescriptorRequest aasDescriptorRequest = getShellDescriptorRequest(partAsPlannedMapper.mapFrom(partAsPlannedEntity));
		ShellDescriptorResponse result = gateway.createShellDescriptor(aasDescriptorRequest);
		shellId = result.getIdentification();
		logDebug(String.format("Shell created with id '%s'", shellId));

		return shellId;
	}

	private ShellLookupRequest getShellLookupRequest(SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect) {
		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(PartAsPlannedConstants.ASSET_LIFECYCLE_PHASE, PartAsPlannedConstants.AS_PLANNED);
		shellLookupRequest.addLocalIdentifier(PartAsPlannedConstants.MANUFACTURER_PART_ID, singleLevelBoMAsPlannedAspect.getParentManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(PartAsPlannedConstants.MANUFACTURER_ID, partAsPlannedConstants.getManufacturerId());

		return shellLookupRequest;
	}

	@SneakyThrows
	private CreateSubModelRequest getCreateSubModelRequest(SingleLevelBoMAsPlanned singleLevelBoMAsPlannedAspect) {
		ArrayList<String> value = new ArrayList<>();
		value.add(getsemanticIdOfModel());
		String identification = UUIdGenerator.getUrnUuid();
		SemanticId semanticId = new SemanticId(value);

		List<Endpoint> endpoints = new ArrayList<>();
		endpoints.add(Endpoint.builder().endpointInterface(PartAsPlannedConstants.HTTP)
				.protocolInformation(ProtocolInformation.builder()
						.endpointAddress(String.format(String.format("%s%s/%s-%s%s", partAsPlannedConstants.getEdcEndpoint().replace("data", ""),
								partAsPlannedConstants.getManufacturerId(), singleLevelBoMAsPlannedAspect.getShellId(), identification,
								"/submodel?content=value&extent=WithBLOBValue")))
						.endpointProtocol(PartAsPlannedConstants.HTTPS)
						.endpointProtocolVersion(PartAsPlannedConstants.ENDPOINT_PROTOCOL_VERSION)
						.build())
				.build());

		return CreateSubModelRequest.builder()
				.idShort(getIdShortOfModel())
				.identification(identification)
				.semanticId(semanticId)
				.endpoints(endpoints)
				.build();
	}

	private ShellDescriptorRequest getShellDescriptorRequest(PartAsPlanned partAsPlannedAspect) {
		ArrayList<KeyValuePair> specificIdentifiers = new ArrayList<>();
		specificIdentifiers.add(new KeyValuePair(PartAsPlannedConstants.ASSET_LIFECYCLE_PHASE, PartAsPlannedConstants.AS_PLANNED));
		specificIdentifiers.add(new KeyValuePair(PartAsPlannedConstants.MANUFACTURER_PART_ID, partAsPlannedAspect.getManufacturerPartId()));
		specificIdentifiers.add(new KeyValuePair(PartAsPlannedConstants.MANUFACTURER_ID, partAsPlannedConstants.getManufacturerId()));

		List<String> values = new ArrayList<>();
		values.add(partAsPlannedAspect.getUuid());
		GlobalAssetId globalIdentifier = new GlobalAssetId(values);

		return ShellDescriptorRequest.builder()
				.idShort(String.format("%s_%s_%s", partAsPlannedAspect.getNameAtManufacturer(), partAsPlannedConstants.getManufacturerId(),
						partAsPlannedAspect.getManufacturerPartId()))
				.globalAssetId(globalIdentifier).specificAssetIds(specificIdentifiers)
				.identification(UUIdGenerator.getUrnUuid()).build();
	}
}