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

        validateAspectData(rowDataFields);

        return Aspect.builder()
                .processId(processId)
                .localIdentifiersKey(rowDataFields[1].trim())
                .localIdentifiersValue(rowDataFields[2].trim())
                .manufacturingDate(rowDataFields[3].trim())
                .manufacturingCountry(rowDataFields[4].trim())
                .manufacturerPartId(rowDataFields[5].trim())
                .customerPartId(rowDataFields[6].trim())
                .classification(rowDataFields[7].trim())
                .nameAtManufacturer(rowDataFields[8].trim())
                .nameAtCustomer(rowDataFields[9].trim())
                .build();
    }

    private void validateAspectData(String[] rowDataFields) {
        String errorMessage = "";
        if (rowDataFields[1].isBlank()) {
            errorMessage = add(errorMessage, "local_identifier_key");
        }
        if (rowDataFields[2].isBlank()) {
            errorMessage = add(errorMessage, "local_identifier_value");
        }
        if (rowDataFields[3].isBlank()) {
            errorMessage = add(errorMessage, "manufacturing_date");
        }
        if (rowDataFields[5].isBlank()) {
            errorMessage = add(errorMessage, "manufacturer_part_id");
        }
        if (rowDataFields[7].isBlank()) {
            errorMessage = add(errorMessage, "classification");
        }
        if (rowDataFields[8].isBlank()) {
            errorMessage = add(errorMessage, "name_at_manufacturer");
        }
        if (!errorMessage.isBlank()) {
            throw new RuntimeException(errorMessage);
        }
    }

    private String add(String errorMessage, String newMessage) {
        return errorMessage.isBlank() ? "Not allowed empty fields: " + newMessage : errorMessage + ", " + newMessage;
    }
}