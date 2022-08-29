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

package com.catenax.dft.usecases.batchs;

import org.springframework.stereotype.Service;

import com.catenax.dft.entities.batch.BatchResponse;
import com.catenax.dft.gateways.database.BatchRepository;
import com.catenax.dft.mapper.BatchMapper;

@Service
public class GetBatchsUseCase {

    private final BatchRepository repository;
    private final BatchMapper mapper;


    public GetBatchsUseCase(BatchRepository repository, BatchMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public BatchResponse execute(String uuid) {

        return mapper.mapToResponse(repository.findByUuid(uuid));
    }
}
