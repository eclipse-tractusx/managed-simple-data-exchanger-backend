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

package com.catenax.dft.usecases.csvHandler.aspectRelationship;

import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvHandler.exceptions.MapToAspectRelationshipException;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.catenax.dft.gateways.file.CsvGateway.SEPARATOR;

@Service
public class MapToAspectRelationshipCsvHandlerUseCase extends AbstractCsvHandlerUseCase<String, AspectRelationship> {

    private final int ROW_LENGTH = 12;

    public MapToAspectRelationshipCsvHandlerUseCase(FetchCatenaXIdCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);

    }

    @Override
    protected AspectRelationship executeUseCase(String rowData, String processId) {
        String[] rowDataFields = rowData.split(SEPARATOR, -1);

        if (rowDataFields.length != ROW_LENGTH) {
            throw new MapToAspectRelationshipException("This row has wrong amount of fields");
        }

        AspectRelationship aspectRelationShip = AspectRelationship.builder()
                .processId(processId)
                .parentPartInstanceId(rowDataFields[0].trim())
                .parentManufactorerPartId(rowDataFields[1].trim())
                .parentOptionalIdentifierKey(rowDataFields[2].trim())
                .parentOptionalIdentifierValue(rowDataFields[3].trim())
                .childPartInstanceId(rowDataFields[4].trim())
                .childManufactorerPartId(rowDataFields[5].trim())
                .childOptionalIdentifierKey(rowDataFields[6].trim())
                .childOptionalIdentifierValue(rowDataFields[7].trim())
                .lifecycleContext(rowDataFields[8].trim())
                .quantityNumber(rowDataFields[9].trim())
                .measurementUnitLexicalValue(rowDataFields[10].trim())
                .assembledOn(rowDataFields[11].trim())
                .build();

        List<String> errorMessages = validateAsset(aspectRelationShip);
        if (errorMessages.size() != 0) {
            throw new MapToAspectRelationshipException(errorMessages.toString());
        }

        return aspectRelationShip;
    }

    private List<String> validateAsset(AspectRelationship asset) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<AspectRelationship>> violations = validator.validate(asset);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
    }
}
