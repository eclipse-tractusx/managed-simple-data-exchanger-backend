package com.catenax.dft.usecases;

import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.database.AspectRepository;
import com.catenax.dft.mapper.AspectEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StoreAspectUseCase extends AbstractUseCase<Aspect, Aspect> {

    private final AspectRepository aspectRepository;
    private final AspectEntityMapper mapper;

    public StoreAspectUseCase(AspectRepository aspectRepository, AspectEntityMapper mapper) {
        super(null);
        this.aspectRepository = aspectRepository;
        this.mapper = mapper;
    }

    protected Aspect executeUseCase(Aspect input) {
        aspectRepository.save(mapper.mapFrom(input));
        log.debug("Aspect store successfully");
        return input;
    }
}