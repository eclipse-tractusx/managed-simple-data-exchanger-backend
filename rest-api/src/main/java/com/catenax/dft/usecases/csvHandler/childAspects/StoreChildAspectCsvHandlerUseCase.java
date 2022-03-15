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

package com.catenax.dft.usecases.csvHandler.childAspects;

import com.catenax.dft.entities.database.ChildAspectEntity;
import com.catenax.dft.entities.usecases.ChildAspect;
import com.catenax.dft.gateways.database.ChildAspectRepository;
import com.catenax.dft.mapper.ChildAspectMapper;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StoreChildAspectCsvHandlerUseCase extends AbstractCsvHandlerUseCase<ChildAspect, ChildAspect> {

    private final ChildAspectRepository repository;
    private final ChildAspectMapper mapper;

    public StoreChildAspectCsvHandlerUseCase(ChildAspectRepository childAspectRepository, ChildAspectMapper mapper) {
        super(null);
        this.repository = childAspectRepository;
        this.mapper = mapper;
    }

    protected ChildAspect executeUseCase(ChildAspect input) {
        ChildAspectEntity entity = mapper.mapFrom(input);

        repository.save(entity);
        log.debug("Aspect store successfully");
        return input;
    }
}