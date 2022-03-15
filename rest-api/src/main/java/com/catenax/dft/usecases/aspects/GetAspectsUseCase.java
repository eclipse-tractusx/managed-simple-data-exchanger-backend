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

package com.catenax.dft.usecases.aspects;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.gateways.database.AspectRepository;
import com.catenax.dft.mapper.AspectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class GetAspectsUseCase {

    private final AspectRepository repository;
    private final AspectMapper mapper;

    public GetAspectsUseCase(AspectRepository repository, AspectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    public Page<AspectEntity> fetchAllAspects(int page, int size) {

        return repository.findAll(PageRequest.of(page, size));
    }
}
