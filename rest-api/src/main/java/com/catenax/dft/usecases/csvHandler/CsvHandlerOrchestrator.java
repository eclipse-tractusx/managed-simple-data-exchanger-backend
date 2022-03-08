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

    public CsvHandlerOrchestrator(MapToAspectCsvHandlerUseCase aspectStarterUseCase, MapToChildAspectCsvHandlerUseCase childAspectStarterUseCase) {
        this.aspectStarterUseCase = aspectStarterUseCase;
        this.childAspectStarterUseCase = childAspectStarterUseCase;
    }

    @SneakyThrows
    public void execute(CsvContent csvContent) {
        if (ASPECT_COLUMNS.equals(csvContent.getColumns())) {
            log.info("I'm an ASPECT file. Unpacked and ready to be processed.");
            csvContent.getRows().parallelStream().forEach(aspectStarterUseCase::run);
        } else if (CHILD_ASPECT_COLUMNS.equals(csvContent.getColumns())) {
            log.info("I'm an CHILD ASPECT file. Unpacked and ready to be processed.");
            csvContent.getRows().parallelStream().forEach(childAspectStarterUseCase::run);
        } else {
            throw new Exception("I don't know what to do with you");
        }
    }
}
