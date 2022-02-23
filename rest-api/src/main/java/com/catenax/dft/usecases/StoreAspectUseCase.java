package com.catenax.dft.usecases;

import com.catenax.dft.entities.Aspect;
import com.catenax.dft.mapper.AspectEntityDaoMapper;
import com.catenax.dft.repository.AspectDaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StoreAspectUseCase extends AbstractUseCase<Aspect, Aspect> {

    private final AspectDaoRepository aspectDaoRepository;

    private final AspectEntityDaoMapper mapper;

    public StoreAspectUseCase(AspectDaoRepository aspectDaoRepository, AspectEntityDaoMapper mapper) {
        super(null);
        this.aspectDaoRepository = aspectDaoRepository;
        this.mapper = mapper;
    }

    protected Aspect executeUseCase(Aspect input) {

        aspectDaoRepository.save(mapper.mapFrom(input));
        log.debug("Aspect store successfully");
        return input;
    }
}