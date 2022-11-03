/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
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

package com.catenax.sde.usecases.csvhandler.batchs;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.stereotype.Service;

import com.catenax.sde.entities.batch.BatchRequest;
import com.catenax.sde.entities.usecases.Batch;
import com.catenax.sde.mapper.BatchMapper;
import com.catenax.sde.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.sde.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;

import lombok.SneakyThrows;

@Service
public class MapFromBatchRequestUseCase extends AbstractCsvHandlerUseCase<BatchRequest, Batch> {
    private final BatchMapper batchMapper;

    public MapFromBatchRequestUseCase(GenerateBatchUuIdCsvHandlerUseCase nextUseCase, BatchMapper mapper) {
        super(nextUseCase);
        this.batchMapper=mapper;
    }

    @SneakyThrows
    @Override
    protected Batch executeUseCase(BatchRequest input, String processId) {
        Batch batch = batchMapper.mapFrom(input);

        List<String> errorMessages = validateAsset(batch);
        if (!errorMessages.isEmpty()) {
            throw new CsvHandlerUseCaseException(input.getRowNumber(), errorMessages.toString());
        }

        return batch;
    }

    private List<String> validateAsset(Batch asset) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<Batch>> violations = validator.validate(asset);

        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();
    }
}