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

import static com.catenax.dft.gateways.file.CsvGateway.SEPARATOR;

@Component
@Slf4j
public class MapToAspectCsvHandlerUseCase extends AbstractCsvHandlerUseCase<String, Aspect> {

    private final int ROW_LENGTH = 11;

    public MapToAspectCsvHandlerUseCase(GenerateUuIdCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    @SneakyThrows
    public Aspect executeUseCase(String rowData, String processId) {

        String[] rowDataFields = rowData.split(SEPARATOR, -1);
        if (rowDataFields.length != ROW_LENGTH) {
            throw new MapToAspectException("This row has the wrong amount of fields");
        }

        validateAspectData(rowDataFields);

        return Aspect.builder()
                .processId(processId)
                .partInstanceId(rowDataFields[1].trim())
                .manufacturingDate(rowDataFields[2].trim())
                .manufacturingCountry(rowDataFields[3].trim())
                .manufacturerPartId(rowDataFields[4].trim())
                .customerPartId(rowDataFields[5].trim())
                .classification(rowDataFields[6].trim())
                .nameAtManufacturer(rowDataFields[7].trim())
                .nameAtCustomer(rowDataFields[8].trim())
                .optionalIdentifierKey(rowDataFields[9].isBlank() ? null : rowDataFields[9])
                .optionalIdentifierValue(rowDataFields[10].isBlank() ? null : rowDataFields[10])
                .build();
    }

    private void validateAspectData(String[] rowDataFields) {
        String errorMessage = "";
        if (rowDataFields[1].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "part_instance_id");
        }
        if (rowDataFields[2].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "manufacturing_date");
        }
        if (rowDataFields[4].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "manufacturer_part_id");
        }
        if (rowDataFields[6].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "classification");
        }
        if (rowDataFields[7].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "name_at_manufacturer");
        }
        if (rowDataFields[9].isBlank() && !rowDataFields[10].isBlank()
                || !rowDataFields[9].isBlank() && rowDataFields[10].isBlank()) {
            errorMessage = addOptionalIdentifierMessage(errorMessage);
        }

        if (!errorMessage.isBlank()) {
            throw new RuntimeException(errorMessage);
        }
    }

    private String addEmptyFieldMessage(String errorMessage, String newMessage) {
        return errorMessage.isBlank() ? "Not allowed empty fields: " + newMessage : errorMessage + ", " + newMessage;
    }

    private String addOptionalIdentifierMessage(String errorMessage) {
        String newMessage = "optional_identifier_key and optional_identifier_value have to be either both null or both filled";
        return errorMessage.isBlank() ? newMessage : "\n" + newMessage;
    }
}