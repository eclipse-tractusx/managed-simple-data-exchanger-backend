package com.catenax.dft.usecases.aspects;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.gateways.database.AspectRepository;
import com.catenax.dft.mapper.AspectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class GetAspectsUseCase {

    private final AspectRepository repository;
    private final AspectMapper mapper;

    public GetAspectsUseCase(AspectRepository repository, AspectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    public Page<AspectEntity> fetchAllAspects(int page, int size) {

        return repository.findAll(PageRequest.of(page, size));
    }
}
