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
package org.eclipse.tractusx.sde.submodels.pap.steps;

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
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlanned;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

@Service
public class DigitalTwinsPartAsPlannedHandlerStep extends Step {

	@Autowired
	private PartAsPlannedConstants partAsPlannedConstants;

	@Autowired
	private DigitalTwinGateway gateway;

	@SneakyThrows
	public PartAsPlanned run(PartAsPlanned partAsPlannedAspect) throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(partAsPlannedAspect);
		} catch (Exception e) {
			throw new ServiceException(partAsPlannedAspect.getRowNumber() + ": DigitalTwins: " + e.getMessage());
		}
	}

	private PartAsPlanned doRun(PartAsPlanned partAsPlannedAspect) throws CsvHandlerDigitalTwinUseCaseException {
		ShellLookupRequest shellLookupRequest = getShellLookupRequest(partAsPlannedAspect);
		ShellLookupResponse shellIds = gateway.shellLookup(shellLookupRequest);

		String shellId;

		if (shellIds.isEmpty()) {
			logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
			ShellDescriptorRequest aasDescriptorRequest = getShellDescriptorRequest(partAsPlannedAspect);
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

		partAsPlannedAspect.setShellId(shellId);
		SubModelListResponse subModelResponse = gateway.getSubModels(shellId);
		SubModelResponse foundSubmodel = null;
		if (subModelResponse != null) {
			foundSubmodel = subModelResponse.stream().filter(x -> getIdShortOfModel().equals(x.getIdShort()))
					.findFirst().orElse(null);
			if (foundSubmodel != null)
				partAsPlannedAspect.setSubModelId(foundSubmodel.getIdentification());
		}

		if (subModelResponse == null || foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(partAsPlannedAspect);
			gateway.createSubModel(shellId, createSubModelRequest);
			partAsPlannedAspect.setSubModelId(createSubModelRequest.getIdentification());
		} else {
			partAsPlannedAspect.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}

		return partAsPlannedAspect;
	}

	private ShellLookupRequest getShellLookupRequest(PartAsPlanned partAsPlannedAspect) {

		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(PartAsPlannedConstants.MANUFACTURER_PART_ID,
				partAsPlannedAspect.getManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(PartAsPlannedConstants.MANUFACTURER_ID,
				partAsPlannedConstants.getManufacturerId());
		shellLookupRequest.addLocalIdentifier(PartAsPlannedConstants.ASSET_LIFECYCLE_PHASE,
				PartAsPlannedConstants.AS_PLANNED);

		return shellLookupRequest;
	}

	private CreateSubModelRequest getCreateSubModelRequest(PartAsPlanned partAsPlannedAspect) {

		ArrayList<String> value = new ArrayList<>();
		value.add(getsemanticIdOfModel());
		SemanticId semanticId = new SemanticId(value);
		String identification = PartAsPlannedConstants.PREFIX + UUID.randomUUID();

		List<Endpoint> endpoints = new ArrayList<>();
		endpoints.add(Endpoint.builder().endpointInterface(PartAsPlannedConstants.HTTP)
				.protocolInformation(ProtocolInformation.builder()
						.endpointAddress(String.format(String.format("%s%s/%s-%s%s",
								partAsPlannedConstants.getEdcEndpoint().replace("data", ""),
								partAsPlannedConstants.getManufacturerId(), partAsPlannedAspect.getShellId(),
								identification, "/submodel?content=value&extent=WithBLOBValue")))
						.endpointProtocol(PartAsPlannedConstants.HTTPS)
						.endpointProtocolVersion(PartAsPlannedConstants.ENDPOINT_PROTOCOL_VERSION).build())
				.build());

		return CreateSubModelRequest.builder().idShort(getIdShortOfModel()).identification(identification)
				.semanticId(semanticId).endpoints(endpoints).build();
	}

	private ShellDescriptorRequest getShellDescriptorRequest(PartAsPlanned partAsPlannedAspect) {

		ArrayList<KeyValuePair> specificIdentifiers = new ArrayList<>();
		specificIdentifiers.add(new KeyValuePair(PartAsPlannedConstants.MANUFACTURER_PART_ID,
				partAsPlannedAspect.getManufacturerPartId()));
		specificIdentifiers.add(
				new KeyValuePair(PartAsPlannedConstants.MANUFACTURER_ID, partAsPlannedConstants.getManufacturerId()));
		specificIdentifiers
				.add(new KeyValuePair(PartAsPlannedConstants.ASSET_LIFECYCLE_PHASE, PartAsPlannedConstants.AS_PLANNED));

		List<String> values = new ArrayList<>();
		values.add(partAsPlannedAspect.getUuid());
		GlobalAssetId globalIdentifier = new GlobalAssetId(values);

		return ShellDescriptorRequest.builder()
				.idShort(String.format("%s_%s_%s", partAsPlannedAspect.getNameAtManufacturer(),
						partAsPlannedConstants.getManufacturerId(), partAsPlannedAspect.getManufacturerPartId()))
				.globalAssetId(globalIdentifier).specificAssetIds(specificIdentifiers)
				.identification(PartAsPlannedConstants.PREFIX + UUID.randomUUID()).build();
	}
}