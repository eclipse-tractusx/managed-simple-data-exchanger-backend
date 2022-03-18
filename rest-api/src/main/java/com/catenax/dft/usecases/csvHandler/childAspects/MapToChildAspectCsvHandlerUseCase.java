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


    public MapToChildAspectCsvHandlerUseCase(CsvHandlerUseCase<ChildAspect> nextUseCase) {
        super(nextUseCase);

    }

    @Override
    protected ChildAspect executeUseCase(String rowData) {
        String[] rowDataFields = rowData.split(SEPARATOR);

        if (rowDataFields.length != 5) {
            throw new RuntimeException("This row has wrong amount of fields");
        }

        return ChildAspect.builder()
                .parentIdentifierKey(rowDataFields[0])
                .parentIdentifierValue(rowDataFields[1])
                .lifecycleContext(rowDataFields[2])
                .quantityNumber(Integer.parseInt(rowDataFields[3]))
                .measurementUnitLexicalValue(rowDataFields[4])
                .build();
    }
}
