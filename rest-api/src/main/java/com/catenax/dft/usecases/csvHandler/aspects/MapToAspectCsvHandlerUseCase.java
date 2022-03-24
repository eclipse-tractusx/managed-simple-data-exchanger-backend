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

    private final int ROW_LENGTH = 9;
    public MapToAspectCsvHandlerUseCase(GenerateUuIdCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    @SneakyThrows
    public Aspect executeUseCase(String rowData, String processId) {
        String[] rowDataFields = rowData.split(SEPARATOR);

        if (rowDataFields.length != ROW_LENGTH){
            throw new MapToAspectException("This row has wrong amount of fields");
        }
        return Aspect.builder()
                .processId(processId)
                .localIdentifiersKey(rowDataFields[0])
                .localIdentifiersValue(rowDataFields[1])
                .manufacturingDate(rowDataFields[2])
                .manufacturingCountry(rowDataFields[3])
                .manufacturerPartId(rowDataFields[4])
                .customerPartId(rowDataFields[5])
                .classification(rowDataFields[6])
                .nameAtManufacturer(rowDataFields[7])
                .nameAtCustomer(rowDataFields[8])
                .build();
    }
}