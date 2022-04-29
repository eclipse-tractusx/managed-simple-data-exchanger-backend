/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.catenax.dft.usecases.csvHandler.aspectRelationship;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.entities.digitalTwins.common.*;
import com.catenax.dft.entities.digitalTwins.request.CreateSubModelRequest;
import com.catenax.dft.entities.digitalTwins.request.ShellDescriptorRequest;
import com.catenax.dft.entities.digitalTwins.request.ShellLookupRequest;
import com.catenax.dft.entities.digitalTwins.response.ShellDescriptorResponse;
import com.catenax.dft.entities.digitalTwins.response.ShellLookupResponse;
import com.catenax.dft.entities.digitalTwins.response.SubModelListResponse;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.gateways.database.AspectRepository;
import com.catenax.dft.gateways.external.DigitalTwinGateway;
import com.catenax.dft.mapper.AspectMapper;
import com.catenax.dft.usecases.common.UUIdGenerator;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvHandler.exceptions.CsvHandlerUseCaseException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DigitalTwinsAspectRelationShipCsvHandlerUseCase extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private static final String PART_INSTANCE_ID = "PartInstanceID";
    private static final String MANUFACTURER_PART_ID = "ManufacturerPartID";
    private static final String MANUFACTURER_ID = "ManufacturerID";
    private static final String HTTP = "HTTP";
    private static final String HTTPS = "HTTPS";
    private static final String SEMANTIC_ID = " urn:bamm:com.catenax.assembly_part_relationship:1.0.0";
    private static final String ID_SHORT = "assemblyPartRelationship";
    private static final String ENDPOINT_PROTOCOL_VERSION = "1.0";

    private final DigitalTwinGateway gateway;
    private final AspectRepository aspectRepository;
    private final AspectMapper aspectMapper;

    @Value(value = "${manufacturerId}")
    private String manufacturerId;
    @Value(value = "${edc.child.aspect.url}")
    private String edcEndpointChildren;

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
        } catch (HttpClientErrorException e) {
            throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(), e.getMessage());
        }
    }

    private AspectRelationship doUseCase(AspectRelationship aspectRelationShip) throws CsvHandlerUseCaseException {
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
            throw new CsvHandlerUseCaseException(aspectRelationShip.getRowNumber(), String.format("Multiple id's found on childAspect %s", shellLookupRequest.toJsonString()));
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
            aspectRelationShip.setSubModelId(subModelResponse.stream()
                    .filter(x -> x.getIdShort().equals(ID_SHORT)).findFirst()
                    .get().getIdentification());
        }
        return aspectRelationShip;
    }

    private String createShellDescriptor(AspectRelationship aspectRelationShip, ShellLookupRequest shellLookupRequest) throws CsvHandlerUseCaseException {
        String shellId;
        logDebug(String.format("No shell id for '%s'", shellLookupRequest.toJsonString()));
        AspectEntity aspectEntity = aspectRepository.findByIdentifiers(
                aspectRelationShip.getParentPartInstanceId(),
                aspectRelationShip.getParentManufacturerPartId(),
                aspectRelationShip.getParentOptionalIdentifierKey(),
                aspectRelationShip.getParentOptionalIdentifierValue());

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

    private CreateSubModelRequest getCreateSubModelRequest(AspectRelationship aspectRelationShip) {
        ArrayList<String> value = new ArrayList<>();
        value.add(SEMANTIC_ID);
        SemanticId semanticId = new SemanticId();
        semanticId.value = value;

        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(Endpoint.builder()
                .endpointInterface(HTTP)
                .protocolInformation(ProtocolInformation.builder()
                        .endpointAddress(String.format(edcEndpointChildren, aspectRelationShip.getParentUuid()))
                        .endpointProtocol(HTTPS)
                        .endpointProtocolVersion(ENDPOINT_PROTOCOL_VERSION)
                        .build())
                .build());

        return CreateSubModelRequest.builder()
                .idShort(ID_SHORT)
                .identification(UUIdGenerator.getUrnUuid())
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