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

import com.catenax.dft.entities.database.AspectRelationshipEntity;
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.gateways.database.AspectRelationshipRepository;
import com.catenax.dft.mapper.AspectRelationshipMapper;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StoreAspectRelationshipCsvHandlerUseCase extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private final AspectRelationshipRepository repository;
    private final AspectRelationshipMapper mapper;

    public StoreAspectRelationshipCsvHandlerUseCase(AspectRelationshipRepository aspectRelationshipRepository, AspectRelationshipMapper mapper) {
        super(null);
        this.repository = aspectRelationshipRepository;
        this.mapper = mapper;
    }

    protected AspectRelationship executeUseCase(AspectRelationship input, String processId) {
        AspectRelationshipEntity entity = mapper.mapFrom(input);
        repository.save(entity);

        return input;
    }
}