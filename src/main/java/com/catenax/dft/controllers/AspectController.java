/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.catenax.dft.controllers;

import com.catenax.dft.entities.aspect.AspectRequest;
import com.catenax.dft.entities.aspect.AspectResponse;
import com.catenax.dft.entities.aspectrelationship.AspectRelationshipResponse;
import com.catenax.dft.usecases.aspectrelationship.GetAspectsRelationshipUseCase;
import com.catenax.dft.usecases.aspects.GetAspectsUseCase;
import com.catenax.dft.usecases.csvhandler.aspects.CreateAspectsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("aspect")
public class AspectController {

    private final GetAspectsUseCase aspectsUseCase;
    private final GetAspectsRelationshipUseCase aspectsRelationshipUseCase;
    private final CreateAspectsUseCase createAspectsUseCase;

    public AspectController(GetAspectsUseCase aspectsUseCase,
                            GetAspectsRelationshipUseCase aspectsRelationshipUseCase,
                            CreateAspectsUseCase createAspectsUseCase) {
        this.aspectsUseCase = aspectsUseCase;
        this.aspectsRelationshipUseCase = aspectsRelationshipUseCase;
        this.createAspectsUseCase = createAspectsUseCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AspectResponse> getAspect(@PathVariable("id") String uuid) {

        AspectResponse response = aspectsUseCase.execute(uuid);

        if (response == null) {
            return notFound().build();
        }
        return ok().body(response);
    }

    @GetMapping(value = "/{id}/relationship", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AspectRelationshipResponse> getAspectRelationships(@PathVariable("id") String uuid) {

        AspectRelationshipResponse response = aspectsRelationshipUseCase.execute(uuid);

        if (response == null) {
            return notFound().build();
        }
        return ok().body(response);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createAspect(@RequestBody List<AspectRequest> aspects) {

        String processId = UUID.randomUUID().toString();

        Runnable runnable = () -> createAspectsUseCase.createAspects(aspects, processId);
        new Thread(runnable).start();

        return ok().body(processId);
    }

}
