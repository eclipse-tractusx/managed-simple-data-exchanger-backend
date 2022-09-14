/********************************************************************************
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

package com.catenax.dft.usecases.csvhandler.batchs;

import org.springframework.stereotype.Service;

import com.catenax.dft.entities.database.BatchEntity;
import com.catenax.dft.entities.usecases.Batch;
import com.catenax.dft.gateways.database.BatchRepository;
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