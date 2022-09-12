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

import org.springframework.stereotype.Service;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.gateways.database.AspectRepository;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FetchCatenaXIdCsvHandlerUseCase
        extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private final AspectRepository repository;

    public FetchCatenaXIdCsvHandlerUseCase(DigitalTwinsAspectRelationShipCsvHandlerUseCase nextUseCase,
                                           AspectRepository repository) {
        super(nextUseCase);
        this.repository = repository;
    }

    @Override
    protected AspectRelationship executeUseCase(AspectRelationship input, String processId) {
        if (input.getParentUuid() == null || input.getParentUuid().isBlank()) {
            String parentUuid = getUuidIfAspectExists(input.getRowNumber(),
                    input.getParentPartInstanceId(),
                    input.getParentManufacturerPartId(),
                    input.getParentOptionalIdentifierKey(),
                    input.getParentOptionalIdentifierValue());
            input.setParentUuid(parentUuid);
        }

        if (input.getChildUuid() == null || input.getChildUuid().isBlank()) {
            String childUuid = getUuidIfAspectExists(input.getRowNumber(),
                    input.getChildPartInstanceId(),
                    input.getChildManufacturerPartId(),
                    input.getChildOptionalIdentifierKey(),
                    input.getChildOptionalIdentifierValue());
            input.setChildUuid(childUuid);
        }

        return input;
    }

    @SneakyThrows
    private String getUuidIfAspectExists(int rowNumber, String partInstanceId, String manufactorerPartId, String optionalIdentifierKey, String optionalIdentifierValue) {
        AspectEntity aspect = repository.findByIdentifiers(
                partInstanceId,
                manufactorerPartId,
                optionalIdentifierKey,
                optionalIdentifierValue);

        if (aspect == null) {
            throw new CsvHandlerUseCaseException(rowNumber,
                    String.format("Missing parent aspect for the given Identifier: PartInstanceID: %s | ManufactorerId: %s | %s: %s",
                            partInstanceId,
                            manufactorerPartId,
                            optionalIdentifierKey,
                            optionalIdentifierValue)
            );
        }
        return aspect.getUuid();
    }
}