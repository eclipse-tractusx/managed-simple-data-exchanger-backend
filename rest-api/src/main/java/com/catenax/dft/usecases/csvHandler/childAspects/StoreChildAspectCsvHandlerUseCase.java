package com.catenax.dft.usecases.csvHandler.childAspects;

import com.catenax.dft.entities.database.ChildAspectEntity;
import com.catenax.dft.entities.usecases.ChildAspect;
import com.catenax.dft.gateways.database.ChildAspectRepository;
import com.catenax.dft.mapper.ChildAspectMapper;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StoreChildAspectCsvHandlerUseCase extends AbstractCsvHandlerUseCase<ChildAspect, ChildAspect> {

    private final ChildAspectRepository repository;
    private final ChildAspectMapper mapper;

    public StoreChildAspectCsvHandlerUseCase(ChildAspectRepository childAspectRepository, ChildAspectMapper mapper) {
        super(null);
        this.repository = childAspectRepository;
        this.mapper = mapper;
    }

    protected ChildAspect executeUseCase(ChildAspect input) {
        ChildAspectEntity entity = mapper.mapFrom(input);

        repository.save(entity);
        log.debug("Aspect store successfully");
        return input;
    }
}