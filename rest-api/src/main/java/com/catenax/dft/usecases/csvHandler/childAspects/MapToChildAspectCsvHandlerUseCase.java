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

import com.catenax.dft.entities.usecases.ChildAspect;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvHandler.CsvHandlerUseCase;
import com.catenax.dft.usecases.csvHandler.exceptions.MapToChildAspectException;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.catenax.dft.gateways.file.CsvGateway.SEPARATOR;

@Service
public class MapToChildAspectCsvHandlerUseCase extends AbstractCsvHandlerUseCase<String, ChildAspect> {

    private final int ROW_LENGTH = 5;

    public MapToChildAspectCsvHandlerUseCase(CsvHandlerUseCase<ChildAspect> nextUseCase) {
        super(nextUseCase);

    }

    @Override
    protected ChildAspect executeUseCase(String rowData, String processId) {
        String[] rowDataFields = rowData.split(SEPARATOR, -1);

        if (rowDataFields.length != ROW_LENGTH) {
            throw new MapToChildAspectException("This row has wrong amount of fields");
        }


        ChildAspect childAspect = ChildAspect.builder()
                .processId(processId)
                .parentIdentifierKey(rowDataFields[0].trim())
                .parentIdentifierValue(rowDataFields[1].trim())
                .lifecycleContext(rowDataFields[2].trim())
                .quantityNumber(rowDataFields[3].trim())
                .measurementUnitLexicalValue(rowDataFields[4].trim())
                .build();

        List<String> errorMessages = validateAsset(childAspect);
        if (errorMessages.size() != 0) {
            throw new MapToChildAspectException(errorMessages.toString());
        }

        return childAspect;
    }

    private List<String> validateAsset(ChildAspect asset) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<ChildAspect>> violations = validator.validate(asset);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
    }
}
