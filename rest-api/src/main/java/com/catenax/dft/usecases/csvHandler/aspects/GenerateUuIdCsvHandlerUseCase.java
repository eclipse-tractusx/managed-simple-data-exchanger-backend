package com.catenax.dft.usecases.csvHandler.aspects;

import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GenerateUuIdCsvHandlerUseCase extends AbstractCsvHandlerUseCase<Aspect, Aspect> {

    public GenerateUuIdCsvHandlerUseCase(StoreAspectCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    @Override
    protected Aspect executeUseCase(Aspect input) {
        input.setUuid(UUID.randomUUID().toString());

        return input;
    }
}