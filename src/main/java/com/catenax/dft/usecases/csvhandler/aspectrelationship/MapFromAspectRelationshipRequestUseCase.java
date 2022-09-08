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
package com.catenax.dft.usecases.csvhandler.aspectrelationship;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.stereotype.Service;

import com.catenax.dft.entities.aspectrelationship.AspectRelationshipRequest;
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.mapper.AspectRelationshipMapper;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;

import lombok.SneakyThrows;

@Service
public class MapFromAspectRelationshipRequestUseCase extends AbstractCsvHandlerUseCase<AspectRelationshipRequest, AspectRelationship> {

    private final AspectRelationshipMapper mapper;

    public MapFromAspectRelationshipRequestUseCase(FetchCatenaXIdCsvHandlerUseCase nextUseCase,
                                                      AspectRelationshipMapper mapper) {
        super(nextUseCase);
        this.mapper = mapper;
    }

    @SneakyThrows
    @Override
    protected AspectRelationship executeUseCase(AspectRelationshipRequest input, String processId) {

        AspectRelationship aspectRelationship = mapper.mapFrom(input);
        List<String> errorMessages = validateAsset(aspectRelationship);
        if (!errorMessages.isEmpty()) {
            throw new CsvHandlerUseCaseException(input.getRowNumber(), errorMessages.toString());
        }

        return aspectRelationship;
    }

    private List<String> validateAsset(AspectRelationship asset) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<AspectRelationship>> violations = validator.validate(asset);

        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();
    }

}
