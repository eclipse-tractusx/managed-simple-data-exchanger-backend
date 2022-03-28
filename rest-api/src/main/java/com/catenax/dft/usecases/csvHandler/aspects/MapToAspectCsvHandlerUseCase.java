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
                .localIdentifiersKey(rowDataFields[1])
                .localIdentifiersValue(rowDataFields[2])
                .manufacturingDate(rowDataFields[3])
                .manufacturingCountry(rowDataFields[4])
                .manufacturerPartId(rowDataFields[5])
                .customerPartId(rowDataFields[6])
                .classification(rowDataFields[7])
                .nameAtManufacturer(rowDataFields[8])
                .nameAtCustomer(rowDataFields[9])
                .build();
    }

    private void validateAspectData(String[] rowDataFields) {
        String errorMessage = "";
        if (isBlank(rowDataFields[1])) {
            errorMessage = add(errorMessage, "local_identifier_key");
        }
        if (isBlank(rowDataFields[2])) {
            errorMessage = add(errorMessage, "local_identifier_value");
        }
        if (isBlank(rowDataFields[3])) {
            errorMessage = add(errorMessage, "manufacturing_date");
        }
        if (isBlank(rowDataFields[5])) {
            errorMessage = add(errorMessage, "manufacturer_part_id");
        }
        if (isBlank(rowDataFields[7])) {
            errorMessage = add(errorMessage, "classification");
        }
        if (isBlank(rowDataFields[8])) {
            errorMessage = add(errorMessage, "name_at_manufacturer");
        }
        if (!isBlank(errorMessage)) {
            throw new RuntimeException(errorMessage);
        }
    }

    private boolean isBlank(String field) {
        return field.trim().length() == 0;
    }

    private String add(String errorMessage, String newMessage) {
        return isBlank(errorMessage) ? "Not allowed empty fields: " + newMessage : errorMessage + ", " + newMessage;
    }
}