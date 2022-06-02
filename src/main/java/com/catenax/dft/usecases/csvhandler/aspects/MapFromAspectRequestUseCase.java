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
package com.catenax.dft.usecases.csvhandler.aspects;

import com.catenax.dft.entities.aspect.AspectRequest;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.mapper.AspectMapper;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MapFromAspectRequestUseCase extends AbstractCsvHandlerUseCase<AspectRequest, Aspect> {
    private final AspectMapper aspectMapper;

    public MapFromAspectRequestUseCase(GenerateUuIdCsvHandlerUseCase nextUseCase, AspectMapper mapper) {
        super(nextUseCase);
        this.aspectMapper=mapper;

    }

    @SneakyThrows
    @Override
    protected Aspect executeUseCase(AspectRequest input, String processId) {
        Aspect aspect = aspectMapper.mapFrom(input);
        List<String> errorMessages = validateAsset(aspect);
        if (errorMessages.size() != 0) {
            throw new CsvHandlerUseCaseException(input.getRowNumber(), errorMessages.toString());
        }

        return aspect;
    }

    private List<String> validateAsset(Aspect asset) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<Aspect>> violations = validator.validate(asset);

        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.toList());
    }
}