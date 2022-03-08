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

        return ChildAspect.builder()
                .parentIdentifierKey(rowDataFields[0])
                .parentIdentifierValue(rowDataFields[1])
                .lifecycleContext(rowDataFields[2])
                .quantityNumber(Integer.parseInt(rowDataFields[3]))
                .measurementUnitLexicalValue(rowDataFields[4])
                .build();
    }
}
