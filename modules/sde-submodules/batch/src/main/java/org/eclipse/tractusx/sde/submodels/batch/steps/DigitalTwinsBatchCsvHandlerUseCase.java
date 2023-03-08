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

package org.eclipse.tractusx.sde.submodels.batch.steps;

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
import org.eclipse.tractusx.sde.submodels.batch.constants.BatchConstants;
import org.eclipse.tractusx.sde.submodels.batch.model.Batch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

@Service
public class DigitalTwinsBatchCsvHandlerUseCase extends Step {

	@Autowired
	private DigitalTwinGateway gateway;

	@Autowired
	private DigitalTwinsUtility digitalTwinsUtility;

	@SneakyThrows
	public Batch run(Batch batch) throws CsvHandlerDigitalTwinUseCaseException {
		try {
			return doRun(batch);
		} catch (Exception e) {
			throw new ServiceException(batch.getRowNumber() + ": DigitalTwins: " + e.getMessage());
		}
	}

	@SneakyThrows
	private Batch doRun(Batch batch) throws CsvHandlerDigitalTwinUseCaseException {
		ShellLookupRequest shellLookupRequest = getShellLookupRequest(batch);
		ShellLookupResponse shellIds = gateway.shellLookup(shellLookupRequest);

		String shellId;

		if (shellIds.isEmpty()) {
			logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
			ShellDescriptorRequest aasDescriptorRequest = getShellDescriptorRequest(batch);
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

		batch.setShellId(shellId);
		SubModelListResponse subModelResponse = gateway.getSubModels(shellId);
		SubModelResponse foundSubmodel = null;
		if (subModelResponse != null) {
			foundSubmodel = subModelResponse.stream().filter(x -> getIdShortOfModel().equals(x.getIdShort())).findFirst()
					.orElse(null);
			if (foundSubmodel != null)
				batch.setSubModelId(foundSubmodel.getIdentification());
		}

		if (subModelResponse == null || foundSubmodel == null) {
			logDebug(String.format("No submodels for '%s'", shellId));
			CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(batch);
			gateway.createSubModel(shellId, createSubModelRequest);
			batch.setSubModelId(createSubModelRequest.getIdentification());
		} else {
			batch.setUpdated(CommonConstants.UPDATED_Y);
			logDebug("Complete Digital Twins Update Update Digital Twins");
		}

		return batch;
	}

	private ShellLookupRequest getShellLookupRequest(Batch batch) {
		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		shellLookupRequest.addLocalIdentifier(BatchConstants.BATCH_ID, batch.getBatchId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_PART_ID, batch.getManufacturerPartId());
		shellLookupRequest.addLocalIdentifier(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId());
		
		return shellLookupRequest;
	}

	private CreateSubModelRequest getCreateSubModelRequest(Batch batch) {
		ArrayList<String> value = new ArrayList<>();
		value.add(getsemanticIdOfModel());
		SemanticId semanticId = new SemanticId(value);
		String identification = CommonConstants.PREFIX + UUID.randomUUID();

		List<Endpoint> endpoints = digitalTwinsUtility.prepareDtEndpoint(batch.getShellId(), identification);

		return CreateSubModelRequest.builder().idShort(getIdShortOfModel()).identification(identification).semanticId(semanticId)
				.endpoints(endpoints).build();
	}

	private ShellDescriptorRequest getShellDescriptorRequest(Batch batch) {
		ArrayList<KeyValuePair> specificIdentifiers = new ArrayList<>();
		setSpecifiers(specificIdentifiers, batch);
		List<String> values = new ArrayList<>();
		values.add(batch.getUuid());
		GlobalAssetId globalIdentifier = new GlobalAssetId(values);

		return ShellDescriptorRequest.builder()
				.idShort(String.format("%s_%s_%s", batch.getNameAtManufacturer(), digitalTwinsUtility.getManufacturerId(),
						batch.getManufacturerPartId()))
				.globalAssetId(globalIdentifier).specificAssetIds(specificIdentifiers)
				.identification(CommonConstants.PREFIX + UUID.randomUUID()).build();
	}

	private void setSpecifiers(final ArrayList<KeyValuePair> specificIdentifiers, Batch batch) {
		specificIdentifiers.add(new KeyValuePair(BatchConstants.BATCH_ID, batch.getBatchId()));
		specificIdentifiers.add(new KeyValuePair(CommonConstants.MANUFACTURER_PART_ID, batch.getManufacturerPartId()));
		specificIdentifiers.add(new KeyValuePair(CommonConstants.MANUFACTURER_ID, digitalTwinsUtility.getManufacturerId()));

	}
}