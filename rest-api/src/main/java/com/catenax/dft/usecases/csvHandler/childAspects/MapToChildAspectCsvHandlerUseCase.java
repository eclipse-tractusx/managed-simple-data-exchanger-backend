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
import org.springframework.stereotype.Service;

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

        validateChildAspectDAta(rowDataFields);

        return ChildAspect.builder()
                .processId(processId)
                .parentIdentifierKey(rowDataFields[0].trim())
                .parentIdentifierValue(rowDataFields[1].trim())
                .lifecycleContext(rowDataFields[2].trim())
                .quantityNumber(Integer.parseInt(rowDataFields[3].trim()))
                .measurementUnitLexicalValue(rowDataFields[4].trim())
                .build();
    }

    private void validateChildAspectDAta(String[] rowDataFields) {
        String errorMessage = "";
        if (isBlank(rowDataFields[0])) {
            errorMessage = add(errorMessage, "parent_identifier_key");
        }
        if (isBlank(rowDataFields[1])) {
            errorMessage = add(errorMessage, "parent_identifier_value");
        }
        if (isBlank(rowDataFields[2])) {
            errorMessage = add(errorMessage, "licycle_context");
        }
        if (isBlank(rowDataFields[3])) {
            errorMessage = add(errorMessage, "quantity_number");
        }
        if (isBlank(rowDataFields[4])) {
            errorMessage = add(errorMessage, "measurement_unit_lexical_value");
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
