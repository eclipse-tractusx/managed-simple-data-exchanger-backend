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

package com.catenax.dft.usecases.csvhandler.batchs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.catenax.dft.entities.digitaltwins.common.Endpoint;
import com.catenax.dft.entities.digitaltwins.common.GlobalAssetId;
import com.catenax.dft.entities.digitaltwins.common.KeyValuePair;
import com.catenax.dft.entities.digitaltwins.common.ProtocolInformation;
import com.catenax.dft.entities.digitaltwins.common.SemanticId;
import com.catenax.dft.entities.digitaltwins.request.CreateSubModelRequest;
import com.catenax.dft.entities.digitaltwins.request.ShellDescriptorRequest;
import com.catenax.dft.entities.digitaltwins.request.ShellLookupRequest;
import com.catenax.dft.entities.digitaltwins.response.ShellDescriptorResponse;
import com.catenax.dft.entities.digitaltwins.response.ShellLookupResponse;
import com.catenax.dft.entities.digitaltwins.response.SubModelListResponse;
import com.catenax.dft.entities.usecases.Batch;
import com.catenax.dft.gateways.external.DigitalTwinGateway;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerDigitalTwinUseCaseException;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;

import lombok.SneakyThrows;

@Service
public class DigitalTwinsBatchCsvHandlerUseCase extends AbstractCsvHandlerUseCase<Batch, Batch> {

    private static final String BATCH_ID = "BatchID";
    private static final String MANUFACTURER_PART_ID = "ManufacturerPartID";
    private static final String MANUFACTURER_ID = "ManufacturerID";
    private static final String HTTP = "HTTP";
    private static final String HTTPS = "HTTPS";
    private static final String SEMANTIC_ID = "urn:bamm:com.catenax.batch:1.0.0#Batch";
    private static final String ID_SHORT = "Batch";
    private static final String ENDPOINT_PROTOCOL_VERSION = "1.0";
    private static final String PREFIX = "urn:uuid:";

    private final DigitalTwinGateway gateway;

    @Value(value = "${manufacturerId}")
    private String manufacturerId;
    @Value(value = "${edc.hostname}")
    private String edcEndpoint;

    public DigitalTwinsBatchCsvHandlerUseCase(DigitalTwinGateway gateway, EDCBatchHandlerUseCase nextUseCase) {
        super(nextUseCase);
        this.gateway = gateway;
    }

    @Override
    @SneakyThrows
	protected Batch executeUseCase(Batch batch, String processId) {
		try {
			return doUseCase(batch);
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(batch.getRowNumber(), "DigitalTwins: " + e.getMessage());
		}
	}

    private Batch doUseCase(Batch batch) throws CsvHandlerDigitalTwinUseCaseException {
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
            throw new CsvHandlerDigitalTwinUseCaseException(String.format("Multiple ids found on aspect %s", shellLookupRequest.toJsonString()));
        }

        batch.setShellId(shellId);
        SubModelListResponse subModelResponse = gateway.getSubModels(shellId);

        if (subModelResponse == null || subModelResponse
                .stream()
                .noneMatch(x -> ID_SHORT.equals(x.getIdShort()))) {
            logDebug(String.format("No submodels for '%s'", shellId));
            CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(batch);
            gateway.createSubModel(shellId, createSubModelRequest);
            batch.setSubModelId(createSubModelRequest.getIdentification());
        } else {
        	throw new CsvHandlerDigitalTwinUseCaseException(String.format("Batch submodels already exist/found with Shell id %s for %s",shellId, shellLookupRequest.toJsonString()));
        }

        return batch;
    }

    private ShellLookupRequest getShellLookupRequest(Batch batch) {
        ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
        shellLookupRequest.addLocalIdentifier(BATCH_ID, batch.getBatchId());
        shellLookupRequest.addLocalIdentifier(MANUFACTURER_PART_ID, batch.getManufacturerPartId());
        shellLookupRequest.addLocalIdentifier(MANUFACTURER_ID, manufacturerId);
        if (batch.hasOptionalIdentifier()) {
            shellLookupRequest.addLocalIdentifier(batch.getOptionalIdentifierKey(), batch.getOptionalIdentifierValue());
        }
        return shellLookupRequest;
    }

    private CreateSubModelRequest getCreateSubModelRequest(Batch batch) {
        ArrayList<String> value = new ArrayList<>();
        value.add(SEMANTIC_ID);
        SemanticId semanticId = new SemanticId(value);
        String identification = PREFIX + UUID.randomUUID();
        
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(Endpoint.builder()
                .endpointInterface(HTTP)
                .protocolInformation(ProtocolInformation.builder()
                        .endpointAddress(String.format(String.format("%s%s/%s-%s%s", edcEndpoint.replace("data", ""), manufacturerId ,batch.getShellId(),identification,"/submodel?content=value&extent=WithBLOBValue")))
                        .endpointProtocol(HTTPS)
                        .endpointProtocolVersion(ENDPOINT_PROTOCOL_VERSION)
                        .build())
                .build());

        return CreateSubModelRequest.builder()
                .idShort(ID_SHORT)
                .identification(identification)
                .semanticId(semanticId)
                .endpoints(endpoints)
                .build();
    }

    private ShellDescriptorRequest getShellDescriptorRequest(Batch batch) {
        ArrayList<KeyValuePair> specificIdentifiers = new ArrayList<>();
        specificIdentifiers.add(new KeyValuePair(BATCH_ID, batch.getBatchId()));
        specificIdentifiers.add(new KeyValuePair(MANUFACTURER_PART_ID, batch.getManufacturerPartId()));
        specificIdentifiers.add(new KeyValuePair(MANUFACTURER_ID, manufacturerId));
        if (batch.hasOptionalIdentifier()) {
            specificIdentifiers.add(new KeyValuePair(batch.getOptionalIdentifierKey(), batch.getOptionalIdentifierValue()));
        }
        List<String> values = new ArrayList<>();
        values.add(batch.getUuid());
        GlobalAssetId globalIdentifier = new GlobalAssetId(values);

        return ShellDescriptorRequest
                .builder()
                .idShort(String.format("%s_%s_%s", batch.getNameAtManufacturer(), manufacturerId, batch.getManufacturerPartId()))
                .globalAssetId(globalIdentifier)
                .specificAssetIds(specificIdentifiers)
                .identification(PREFIX + UUID.randomUUID())
                .build();
    }
}