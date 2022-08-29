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

package com.catenax.dft.usecases.csvhandler.batchs;

import org.springframework.stereotype.Service;

import com.catenax.dft.entities.database.BatchEntity;
import com.catenax.dft.entities.usecases.Batch;
import com.catenax.dft.gateways.database.BatchRepository;
import com.catenax.dft.mapper.AspectMapper;
import com.catenax.dft.mapper.BatchMapper;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StoreBatchCsvHandlerUseCase extends AbstractCsvHandlerUseCase<Batch, Batch> {

    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;

    public StoreBatchCsvHandlerUseCase(BatchRepository batchRepository, BatchMapper mapper) {
        super(null);
        this.batchRepository = batchRepository;
        this.batchMapper = mapper;
    }

    protected Batch executeUseCase(Batch input, String processId) {
        BatchEntity entity = batchMapper.mapFrom(input);
        batchRepository.save(entity);

        return input;
    }
}