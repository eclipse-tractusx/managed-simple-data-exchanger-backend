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
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class FetchCatenaXIdCsvHandlerUseCase extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private AspectRepository repository;

    public FetchCatenaXIdCsvHandlerUseCase(DigitalTwinsAspectRelationShipCsvHandlerUseCase nextUseCase, AspectRepository repository) {
        super(nextUseCase);
        this.repository = repository;
    }

    @SneakyThrows
    @Override
    protected AspectRelationship executeUseCase(AspectRelationship input, String processId) {

        AspectEntity parentAspect = repository.findByLocalIdentifiersValueAndManufacturerPartId(input.getParentPartInstanceId(), input.getParentManufactorerPartId());
        AspectEntity childAspect = repository.findByLocalIdentifiersValueAndManufacturerPartId(input.getChildPartInstanceId(), input.getChildManufactorerPartId());

        if (parentAspect == null || childAspect == null) {
            throw new Exception("There is no aspect registred");
        }

        input.setChildUuid(childAspect.getUuid());
        input.setParentUuid(parentAspect.getUuid());

        return input;
    }
}
