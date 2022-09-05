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

import com.catenax.dft.entities.usecases.Batch;
import com.catenax.dft.usecases.common.UUIdGenerator;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;

@Service
public class GenerateBatchUuIdCsvHandlerUseCase extends AbstractCsvHandlerUseCase<Batch, Batch> {

    public GenerateBatchUuIdCsvHandlerUseCase(DigitalTwinsBatchCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    @Override
    protected Batch executeUseCase(Batch input, String processId) {
        if (input.getUuid() == null || input.getUuid().isBlank()) {
            input.setUuid(UUIdGenerator.getUrnUuid());
        }
        return input;
    }
}