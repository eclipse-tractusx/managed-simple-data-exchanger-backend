package com.catenax.dft.usecases.csvHandler;

import com.catenax.dft.entities.usecases.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GenerateUuIdCsvHandlerUseCase extends AbstractCsvHandlerUseCase<Aspect, Aspect> {

    public GenerateUuIdCsvHandlerUseCase(StoreAspectCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    @Override
    protected Aspect executeUseCase(Aspect input) {
        input.uuid = UUID.randomUUID().toString();

        return input;
    }
}