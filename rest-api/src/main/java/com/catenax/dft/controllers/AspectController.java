package com.catenax.dft.controllers;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.usecases.aspects.GetAspectsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController

public class AspectController {

    private final GetAspectsUseCase useCase;

    public AspectController(GetAspectsUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping(path = "/aspect")
    public ResponseEntity<Page<AspectEntity>> getAspects(@Param("page") Integer page, @Param("pageSize") Integer pageSize) {

        page = page == null ? 0 : page;
        pageSize = pageSize == null ? 10 : pageSize;
        return ok().body(useCase.fetchAllAspects(page, pageSize));
    }
}
