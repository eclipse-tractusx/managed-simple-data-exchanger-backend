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

package com.catenax.dft.usecases.csvHandler.aspects;

import com.catenax.dft.entities.digitalTwins.common.*;
import com.catenax.dft.entities.digitalTwins.request.*;
import com.catenax.dft.entities.digitalTwins.response.ShellDescriptorResponse;
import com.catenax.dft.entities.digitalTwins.response.ShellLookupResponse;
import com.catenax.dft.entities.digitalTwins.response.SubModelListResponse;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.external.DigitalTwinGateway;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class DigitalTwinsAspectCsvHandlerUseCase extends AbstractCsvHandlerUseCase<Aspect, Aspect> {

    public static final String PART_INSTANCE_ID = "PartInstanceID";
    public static final String MANUFACTURER_PART_ID = "ManufacturerPartID";
    public static final String MANUFACTURER_ID = "ManufacturerID";
    public static final String HTTP = "HTTP";
    public static final String HTTPS = "HTTPS";
    public static final String SERIAL_PART_TYPIZATION_1_0_0 = "urn:bamm:com.catenax.serial_part_typization:1.0.0";
    public static final String SERIAL_PART_TYPIZATION_ID_SHORT = "serialPartTypization";
    public static final String ENDPOINT_PROTOCOL_VERSION = "1.0";

    private final DigitalTwinGateway gateway;

    @Value(value = "${manufacturerId}")
    private String manufacturerId;
    @Value(value = "${edc.aspect.url}")
    private String edcEndpoint;

    public DigitalTwinsAspectCsvHandlerUseCase(DigitalTwinGateway gateway, StoreAspectCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
        this.gateway = gateway;
    }

    @Override
    @SneakyThrows
    protected Aspect executeUseCase(Aspect aspect,String processId) {
        ShellLookupRequest shellLookupRequest = createLookupRequest(aspect);
        ShellLookupResponse shellIds = gateway.shellLookup(shellLookupRequest);

        String shellId;

        if (shellIds.isEmpty()) {
            log.debug(String.format("[DigitalTwinsAspectCsvHandlerUseCase] No shell id for '%s'", shellLookupRequest.toJsonString()));

            ShellDescriptorRequest aasDescriptorRequest = createShellDescriptorRequest(aspect);
            ShellDescriptorResponse result = gateway.createShellDescriptor(aasDescriptorRequest);
            shellId = result.getIdentification();

            log.debug(String.format("[DigitalTwinsAspectCsvHandlerUseCase] Shell created with id '%s'", shellId));

        } else if (shellIds.size() == 1) {
            log.debug(String.format("[DigitalTwinsAspectCsvHandlerUseCase] Shell id found for '%s'", shellLookupRequest.toJsonString()));

            shellId = shellIds.stream().findFirst().orElse(null);

            log.debug(String.format("[DigitalTwinsAspectCsvHandlerUseCase] Shell id '%s'", shellId));
        } else {
            throw new Exception(String.format("Multiple ids found on aspect %s - %s", aspect.getLocalIdentifiersKey(), aspect.getLocalIdentifiersValue()));
        }

        SubModelListResponse subModelResponse = gateway.getSubModel(shellId);

        if (subModelResponse == null) {
            CreateSubModelRequest createSubModelRequest = getCreateSubModelRequest(aspect);
            gateway.createSubModel(shellId, createSubModelRequest);
        }

        return aspect;
    }

    private ShellLookupRequest createLookupRequest(Aspect aspect) {
        ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
        shellLookupRequest.addLocalIdentifier(PART_INSTANCE_ID, aspect.getLocalIdentifiersValue());
        shellLookupRequest.addLocalIdentifier(MANUFACTURER_PART_ID, aspect.getManufacturerPartId());
        shellLookupRequest.addLocalIdentifier(MANUFACTURER_ID, manufacturerId);
        return shellLookupRequest;
    }

    private CreateSubModelRequest getCreateSubModelRequest(Aspect aspect) {
        List<Description> descriptions = new ArrayList<>();
        descriptions.add(Description.builder()
                .language("en")
                .text("provides base information")
                .build());

        ArrayList<String> value = new ArrayList<>();
        value.add(SERIAL_PART_TYPIZATION_1_0_0);
        SemanticId semanticId = new SemanticId();
        semanticId.value = value;

        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(Endpoint.builder()
                .endpointInterface(HTTP)
                .protocolInformation(ProtocolInformation.builder()
                        .endpointAddress(String.format("%s%s", edcEndpoint, aspect.getUuid()))
                        .endpointProtocol(HTTPS)
                        .endpointProtocolVersion(ENDPOINT_PROTOCOL_VERSION)
                        .build())
                .build());
        return CreateSubModelRequest.builder()
                .description(descriptions)
                .idShort(SERIAL_PART_TYPIZATION_ID_SHORT)
                .identification(aspect.getUuid())
                .semanticId(semanticId)
                .endpoints(endpoints)
                .build();
    }

    private ShellDescriptorRequest createShellDescriptorRequest(Aspect aspect) {
        ArrayList<KeyValuePair> specificIdentifiers = new ArrayList<>();
        specificIdentifiers.add(new KeyValuePair(PART_INSTANCE_ID, aspect.getLocalIdentifiersValue()));
        specificIdentifiers.add(new KeyValuePair(MANUFACTURER_PART_ID, aspect.getManufacturerPartId()));
        specificIdentifiers.add(new KeyValuePair(MANUFACTURER_ID, manufacturerId));

        List<String> values = new ArrayList<>();
        values.add(aspect.getUuid());
        GlobalAssetId globalIdentifier = new GlobalAssetId(values);

        List<Description> descriptions = new ArrayList<>();
        descriptions.add(Description.builder()
                .language("en")
                .text("provides base information")
                .build());

        return ShellDescriptorRequest
                .builder()
                .idShort("1")
                .description(descriptions)
                .globalAssetId(globalIdentifier)
                .specificAssetIds(specificIdentifiers)
                .identification(UUID.randomUUID().toString())
                .build();
    }
}