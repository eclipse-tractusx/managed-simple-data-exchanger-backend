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

    public MapToAspectCsvHandlerUseCase(GenerateUuIdCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    @SneakyThrows
    public Aspect executeUseCase(String rowData) {
        String[] rowDataFields = rowData.split(SEPARATOR);

        return Aspect.builder()
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
