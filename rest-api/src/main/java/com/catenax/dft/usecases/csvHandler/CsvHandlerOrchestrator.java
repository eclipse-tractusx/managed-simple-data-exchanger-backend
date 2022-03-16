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

package com.catenax.dft.usecases.csvHandler;


import com.catenax.dft.entities.csv.CsvContent;
import com.catenax.dft.usecases.csvHandler.aspects.MapToAspectCsvHandlerUseCase;
import com.catenax.dft.usecases.csvHandler.childAspects.MapToChildAspectCsvHandlerUseCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CsvHandlerOrchestrator {

    private final MapToAspectCsvHandlerUseCase aspectStarterUseCase;
    private final MapToChildAspectCsvHandlerUseCase childAspectStarterUseCase;

    private final Set<String> ASPECT_COLUMNS = Stream.of(
            "local_identifiers_key",
            "local_identifiers_value",
            "manufacturing_date",
            "manufacturing_country",
            "manufacturer_part_id",
            "customer_part_id",
            "classification",
            "name_at_manufacturer",
            "name_at_customer")
            .collect(Collectors.toSet());
    private final Set<String> CHILD_ASPECT_COLUMNS = Stream.of(
            "parent_identifier_key",
            "parent_identifier_value",
            "lifecycle_context",
            "quantity_number",
            "measurement_unit_lexical_value")
            .collect(Collectors.toSet());

    //new

    public CsvHandlerOrchestrator(MapToAspectCsvHandlerUseCase aspectStarterUseCase, MapToChildAspectCsvHandlerUseCase childAspectStarterUseCase) {
        this.aspectStarterUseCase = aspectStarterUseCase;
        this.childAspectStarterUseCase = childAspectStarterUseCase;
    }

    @SneakyThrows
    public void execute(CsvContent csvContent, String processId) {
        if (ASPECT_COLUMNS.equals(csvContent.getColumns())) {
            //guardar bd process in progress... first timestamp e nº linhas
            log.info("I'm an ASPECT file. Unpacked and ready to be processed.");
            csvContent.getRows().parallelStream().forEach(aspectStarterUseCase::run);
            //guardar completed, data fim e os restantes elementos
        } else if (CHILD_ASPECT_COLUMNS.equals(csvContent.getColumns())) {
            //guardar bd process in progress... first timestamp e nº linhas
            log.info("I'm an CHILD ASPECT file. Unpacked and ready to be processed.");
            csvContent.getRows().parallelStream().forEach(childAspectStarterUseCase::run);
            //guardar completed, data fim e os restantes elementos
        } else {
            //unknow id, failed, data
            throw new Exception("I don't know what to do with you");
        }
    }
}
