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
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FetchCatenaXIdCsvHandlerUseCase extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private AspectRepository repository;

    public FetchCatenaXIdCsvHandlerUseCase(DigitalTwinsAspectRelationShipCsvHandlerUseCase nextUseCase, AspectRepository repository) {
        super(nextUseCase);
        this.repository = repository;
    }

    @SneakyThrows
    @Override
    protected AspectRelationship executeUseCase(AspectRelationship input, String processId) {
        if (input.getParentUuid() == null || input.getParentUuid().isBlank()) {
            AspectEntity parentAspect = repository.findByIdentifiers(
                    input.getParentPartInstanceId(),
                    input.getParentManufactorerPartId(),
                    input.getParentOptionalIdentifierKey(),
                    input.getParentOptionalIdentifierValue());

            if (parentAspect == null) {
                throw new CsvHandlerUseCaseException(input.getRowNumber(), String.format("Missing parent aspect for the given Identifier: PartInstanceID: %s | ManufactorerId: %s | %s: %s", input.getParentPartInstanceId(),
                        input.getParentManufactorerPartId(),
                        input.getParentOptionalIdentifierKey(),
                        input.getParentOptionalIdentifierValue()));
            }

            input.setChildUuid(parentAspect.getUuid());
        }

        if (input.getChildUuid() == null || input.getChildUuid().isBlank()) {
            AspectEntity childAspect = repository.findByIdentifiers(
                    input.getParentPartInstanceId(),
                    input.getParentManufactorerPartId(),
                    input.getParentOptionalIdentifierKey(),
                    input.getParentOptionalIdentifierValue());

            if (childAspect == null) {
                throw new CsvHandlerUseCaseException(input.getRowNumber(), String.format("Missing child aspect for the given Identifier: PartInstanceID: %s | ManufactorerId: %s | %s: %s", input.getParentPartInstanceId(),
                        input.getChildManufactorerPartId(),
                        input.getChildOptionalIdentifierKey(),
                        input.getChildOptionalIdentifierValue()));
            }
            input.setChildUuid(childAspect.getUuid());
        }
        return input;
    }
}