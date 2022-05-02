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
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.gateways.database.AspectRepository;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvHandler.exceptions.CsvHandlerUseCaseException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FetchCatenaXIdCsvHandlerUseCase extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private final AspectRepository repository;

    public FetchCatenaXIdCsvHandlerUseCase(DigitalTwinsAspectRelationShipCsvHandlerUseCase nextUseCase, AspectRepository repository) {
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