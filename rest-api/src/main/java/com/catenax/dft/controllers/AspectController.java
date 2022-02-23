package com.catenax.dft.controllers;

import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.usecases.GetAspectsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController

public class AspectController {

    private GetAspectsUseCase useCase;

    public AspectController(GetAspectsUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping(path = "/aspect")
    public ResponseEntity<List<Aspect>> getAspects(){

        return ok().body(useCase.fetchAllAspects());
    }

}
