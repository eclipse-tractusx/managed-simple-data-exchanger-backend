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

package com.catenax.dft.usecases.csvhandler.aspectrelationship;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.catenax.dft.entities.database.AspectEntity;
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
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.gateways.database.AspectRepository;
import com.catenax.dft.gateways.external.DigitalTwinGateway;
import com.catenax.dft.mapper.AspectMapper;
import com.catenax.dft.usecases.common.UUIdGenerator;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerDigitalTwinUseCaseException;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DigitalTwinsAspectRelationShipCsvHandlerUseCase
        extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private static final String PART_INSTANCE_ID = "PartInstanceID";
    private static final String MANUFACTURER_PART_ID = "ManufacturerPartID";
    private static final String MANUFACTURER_ID = "ManufacturerID";
    private static final String HTTP = "HTTP";
    private static final String HTTPS = "HTTPS";
    private static final String SEMANTIC_ID = "urn:bamm:io.catenax.assembly_part_relationship:1.1.0#AssemblyPartRelationship";
    private static final String ID_SHORT = "assemblyPartRelationship";
    private static final String ENDPOINT_PROTOCOL_VERSION = "1.0";

    private final DigitalTwinGateway gateway;
    private final AspectRepository aspectRepository;
    private final AspectMapper aspectMapper;

    @Value(value = "${manufacturerId}")
    private String manufacturerId;
    @Value(value = "${edc.hostname}")
    private String edcEndpoint;

    public DigitalTwinsAspectRelationShipCsvHandlerUseCase(DigitalTwinGateway gateway,
                                                           AspectRepository aspectRepository,
                                                           AspectMapper aspectMapper,
                                                           EDCAspectRelationshipHandlerUseCase nextUseCase) {
        super(nextUseCase);
        this.gateway = gateway;
        this.aspectRepository = aspectRepository;
        this.aspectMapper = aspectMapper;
    }

    @Override
    @SneakyThrows
	protected AspectRelationship executeUseCase(AspectRelationship aspectRelationShip, String processId) {
		try {
			return doUseCase(aspectRelationShip);
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(), "DigitalTwins: " + e.getMessage());
		}
	}

    private AspectRelationship doUseCase(AspectRelationship aspectRelationShip) throws CsvHandlerUseCaseException, CsvHandlerDigitalTwinUseCaseException {
        ShellLookupRequest shellLookupRequest = getShellLookupRequest(aspectRelationShip);
        ShellLookupResponse shellIds = gateway.shellLookup(shellLookupRequest);

        String shellId;

        if (shellIds.isEmpty()) {
            shellId = createShellDescriptor(aspectRelationShip, shellLookupRequest);
        } else if (shellIds.size() == 1) {
            logDebug(String.format("Shell id found for '%s'", shellLookupRequest.toJsonString()));
            shellId = shellIds.stream().findFirst().orElse(null);
            logDebug(String.format("Shell id '%s'", shellId));
        } else {
            throw new CsvHandlerDigitalTwinUseCaseException(String.format("Multiple id's found on childAspect %s", shellLookupRequest.toJsonString()));
        }

        aspectRelationShip.setShellId(shellId);
        SubModelListResponse subModelResponse = gateway.getSubModels(shellId);

        if (subModelResponse == null || subModelResponse
                .stream()
                .noneMatch(x -> ID_SHORT.equals(x.getIdShort()))) {
            logDebug(String.format("No submodels for '%s'", shellId));
            CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(aspectRelationShip);
            gateway.createSubModel(shellId, createSubModelRequest);
            aspectRelationShip.setSubModelId(createSubModelRequest.getIdentification());
        } else {
        	throw new CsvHandlerDigitalTwinUseCaseException(String.format("AssemblyPartRelationship submodels already exist/found with Shell id %s for %s",shellId, shellLookupRequest.toJsonString()));
        }
        return aspectRelationShip;
    }

    private String createShellDescriptor(AspectRelationship aspectRelationShip, ShellLookupRequest shellLookupRequest) throws CsvHandlerUseCaseException {
        String shellId;
        logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
        AspectEntity aspectEntity = null;
        if (aspectRelationShip.hasOptionalParentIdentifier()) {
            aspectEntity = aspectRepository.findByIdentifiers(
                    aspectRelationShip.getParentPartInstanceId(),
                    aspectRelationShip.getParentManufacturerPartId(),
                    aspectRelationShip.getParentOptionalIdentifierKey(),
                    aspectRelationShip.getParentOptionalIdentifierValue());
        } else {
            aspectRepository.findByIdentifiers(
                    aspectRelationShip.getParentPartInstanceId(),
                    aspectRelationShip.getParentManufacturerPartId());
        }

        if (aspectEntity == null) {
            throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(), "No parent aspect found");
        }

        ShellDescriptorRequest aasDescriptorRequest = getShellDescriptorRequest(aspectMapper.mapFrom(aspectEntity));
        ShellDescriptorResponse result = gateway.createShellDescriptor(aasDescriptorRequest);
        shellId = result.getIdentification();
        logDebug(String.format("Shell created with id '%s'", shellId));

        return shellId;
    }

    private ShellLookupRequest getShellLookupRequest(AspectRelationship aspectRelationShip) {
        ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
        shellLookupRequest.addLocalIdentifier(PART_INSTANCE_ID, aspectRelationShip.getParentPartInstanceId());
        shellLookupRequest.addLocalIdentifier(MANUFACTURER_PART_ID, aspectRelationShip.getParentManufacturerPartId());
        shellLookupRequest.addLocalIdentifier(MANUFACTURER_ID, manufacturerId);

        if (aspectRelationShip.hasOptionalParentIdentifier()) {
            shellLookupRequest.addLocalIdentifier(aspectRelationShip.getParentOptionalIdentifierKey(), aspectRelationShip.getParentOptionalIdentifierValue());
        }

        return shellLookupRequest;
    }

    @SneakyThrows
    private CreateSubModelRequest getCreateSubModelRequest(AspectRelationship aspectRelationShip) {
        ArrayList<String> value = new ArrayList<>();
        value.add(SEMANTIC_ID);
        String identification = UUIdGenerator.getUrnUuid();
        SemanticId semanticId = new SemanticId(value);

        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(Endpoint.builder()
                .endpointInterface(HTTP)
                .protocolInformation(ProtocolInformation.builder()
                		.endpointAddress(String.format(String.format("%s%s/%s-%s%s", edcEndpoint.replace("data", ""), manufacturerId ,aspectRelationShip.getShellId(),identification,"/submodel?content=value&extent=WithBLOBValue")))
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

    private ShellDescriptorRequest getShellDescriptorRequest(Aspect aspect) {
        ArrayList<KeyValuePair> specificIdentifiers = new ArrayList<>();
        specificIdentifiers.add(new KeyValuePair(PART_INSTANCE_ID, aspect.getPartInstanceId()));
        specificIdentifiers.add(new KeyValuePair(MANUFACTURER_PART_ID, aspect.getManufacturerPartId()));
        specificIdentifiers.add(new KeyValuePair(MANUFACTURER_ID, manufacturerId));

        List<String> values = new ArrayList<>();
        values.add(aspect.getUuid());
        GlobalAssetId globalIdentifier = new GlobalAssetId(values);

        return ShellDescriptorRequest
                .builder()
                .idShort(String.format("%s_%s_%s", aspect.getNameAtManufacturer(), manufacturerId, aspect.getManufacturerPartId()))
                .globalAssetId(globalIdentifier)
                .specificAssetIds(specificIdentifiers)
                .identification(UUIdGenerator.getUrnUuid())
                .build();
    }
}