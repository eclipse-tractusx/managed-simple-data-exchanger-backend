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

package com.catenax.dft.usecases.csvHandler.aspects;

import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.UUID;

import static com.catenax.dft.gateways.file.CsvGateway.SEPARATOR;

@Component
@Slf4j
public class MapToAspectCsvHandlerUseCase extends AbstractCsvHandlerUseCase<String, Aspect> {

    private final int ROW_LENGTH = 10;

    public MapToAspectCsvHandlerUseCase(GenerateUuIdCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    @SneakyThrows
    public Aspect executeUseCase(String rowData, String processId) {

        String[] rowDataFields = rowData.split(SEPARATOR, -1);
        if (rowDataFields.length != ROW_LENGTH) {
            throw new MapToAspectException("This row has the wrong amount of fields");
        }

        Aspect aspect = Aspect.builder()
                .processId(processId)
                .localIdentifiersKey(rowDataFields[1].trim())
                .localIdentifiersValue(rowDataFields[2].trim())
                .manufacturingDate(rowDataFields[3].trim())
                .manufacturingCountry(rowDataFields[4].trim().isBlank() ? null : rowDataFields[4].trim())
                .manufacturerPartId(rowDataFields[5].trim())
                .customerPartId(rowDataFields[6].trim().isBlank() ? null : rowDataFields[6].trim())
                .classification(rowDataFields[7].trim())
                .nameAtManufacturer(rowDataFields[8].trim())
                .nameAtCustomer(rowDataFields[9].trim().isBlank() ? null : rowDataFields[9].trim())
                .build();

        List<String> errorMessages = validateAsset(aspect);
        if (errorMessages.size() != 0) {
            throw new MapToAspectException(errorMessages.toString());
        }

        return aspect;
    }

    private List<String> validateAsset(Aspect asset) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<Aspect>> violations = validator.validate(asset);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
    }
}