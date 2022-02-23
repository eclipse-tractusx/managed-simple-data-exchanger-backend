package com.catenax.dft.usecases;

import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.database.AspectRepository;
import com.catenax.dft.mapper.AspectEntityMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetAspectsUseCase {

    private AspectRepository repository;
    private AspectEntityMapper mapper;

    public GetAspectsUseCase(AspectRepository repository, AspectEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    public List<Aspect> fetchAllAspects() {
        return repository.findAll().stream().map(mapper::mapFrom).collect(Collectors.toList());
    }
}
