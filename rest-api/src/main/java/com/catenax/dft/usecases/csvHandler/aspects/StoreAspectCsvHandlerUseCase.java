package com.catenax.dft.usecases.csvHandler.aspects;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.entities.database.ChildAspectEntity;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.database.AspectRepository;
import com.catenax.dft.mapper.AspectMapper;
import com.catenax.dft.mapper.ChildAspectMapper;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StoreAspectCsvHandlerUseCase extends AbstractCsvHandlerUseCase<Aspect, Aspect> {

    private final AspectRepository aspectRepository;
    private final AspectMapper aspectMapper;
    private final ChildAspectMapper childAspectMapper;

    public StoreAspectCsvHandlerUseCase(AspectRepository aspectRepository, AspectMapper mapper, ChildAspectMapper childAspectMapper) {
        super(null);
        this.aspectRepository = aspectRepository;
        this.aspectMapper = mapper;
        this.childAspectMapper = childAspectMapper;
    }

    protected Aspect executeUseCase(Aspect input) {
        AspectEntity entity = aspectMapper.mapFrom(input);

        aspectRepository.save(entity);
        log.debug("Aspect store successfully");
        return input;
    }
}