package com.catenax.dft.usecases;

import com.catenax.dft.entities.Aspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StoreAspectUseCase extends AbstractUseCase<Aspect, Aspect> {

    public StoreAspectUseCase() {
        super(null);
    }

    protected Aspect executeUseCase(Aspect input) {
        log.info("look at me mama...I'm a fake storage use case");
        return input;
    }
}