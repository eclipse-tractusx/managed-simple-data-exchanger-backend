package com.catenax.dft.usecases;

import com.catenax.dft.entities.usecases.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GenerateUuIdUseCase extends AbstractUseCase<Aspect, Aspect> {

    public GenerateUuIdUseCase(StoreAspectUseCase nextUseCase) {
        super(nextUseCase);
    }

    @Override
    protected Aspect executeUseCase(Aspect input) {
        input.uuid = UUID.randomUUID().toString();

        return input;
    }
}