package com.catenax.dft.usecases;

import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.usecases.exceptions.UseCaseException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MapToAspectUseCase extends AbstractUseCase<String, Aspect> {

    public MapToAspectUseCase(GenerateUuIdUseCase nextUseCase) {
        super(nextUseCase);
    }

    @SneakyThrows
    public Aspect executeUseCase(String rowData) {
        String[] rowDataFields = rowData.split(";");
        if (rowDataFields.length != 6) {
            throw new UseCaseException("MapToAspect", "missing columns in row");
        }
        return Aspect.builder()
                .id(Integer.parseInt(rowDataFields[0]))
                .firstName(rowDataFields[1])
                .lastName(rowDataFields[2])
                .email(rowDataFields[3])
                .email2(rowDataFields[4])
                .profession(rowDataFields[5])
                .build();
    }
}
