/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package com.catenax.dft.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catenax.dft.entities.SubmodelJsonRequest;
import com.catenax.dft.entities.aspect.AspectRequest;
import com.catenax.dft.entities.aspect.AspectResponse;
import com.catenax.dft.entities.aspectrelationship.AspectRelationshipRequest;
import com.catenax.dft.entities.aspectrelationship.AspectRelationshipResponse;
import com.catenax.dft.exceptions.DftException;
import com.catenax.dft.usecases.aspectrelationship.GetAspectsRelationshipUseCase;
import com.catenax.dft.usecases.aspects.GetAspectsUseCase;
import com.catenax.dft.usecases.csvhandler.aspectrelationship.CreateAspectRelationshipUseCase;
import com.catenax.dft.usecases.csvhandler.aspects.CreateAspectsUseCase;

import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("aspect")
@Validated
public class AspectController {

    private final GetAspectsUseCase aspectsUseCase;
    private final GetAspectsRelationshipUseCase aspectsRelationshipUseCase;
    private final CreateAspectsUseCase createAspectsUseCase;
    private final CreateAspectRelationshipUseCase createAspectRelationshipUseCase;

    public AspectController(GetAspectsUseCase aspectsUseCase,
                            GetAspectsRelationshipUseCase aspectsRelationshipUseCase,
                            CreateAspectsUseCase createAspectsUseCase,
                            CreateAspectRelationshipUseCase createAspectRelationshipUseCase) {
        this.aspectsUseCase = aspectsUseCase;
        this.aspectsRelationshipUseCase = aspectsRelationshipUseCase;
        this.createAspectsUseCase = createAspectsUseCase;
        this.createAspectRelationshipUseCase = createAspectRelationshipUseCase;
    }

    @GetMapping(value="/public/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AspectResponse> getAspect(@PathVariable("id") String uuid) {
        AspectResponse response = aspectsUseCase.execute(uuid);

        if (response == null) {
            return notFound().build();
        }
        return ok().body(response);
    }

    @GetMapping(value = "/public/{id}/relationship", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AspectRelationshipResponse> getAspectRelationships(@PathVariable("id") String uuid) {
        AspectRelationshipResponse response = aspectsRelationshipUseCase.execute(uuid);

        if (response == null) {
            return notFound().build();
        }
        return ok().body(response);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createAspect(@RequestBody @Valid SubmodelJsonRequest<AspectRequest> aspects) {
        String processId = UUID.randomUUID().toString();

        Runnable runnable = () -> {
            try {
                createAspectsUseCase.createAspects(aspects, processId);
            } catch (JsonProcessingException e) {
                throw new DftException(e);
            }
        };
        new Thread(runnable).start();

        return ok().body(processId);
    }

    @PostMapping(value="/relationship", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createAspectRelationship(@RequestBody @Valid SubmodelJsonRequest<AspectRelationshipRequest> aspects){
        String processId = UUID.randomUUID().toString();

        Runnable runnable = () -> {
            try {
                createAspectRelationshipUseCase.createAspects(aspects, processId);
            } catch (JsonProcessingException e) {
                throw new DftException(e);
            }
        };
        new Thread(runnable).start();

        return ok().body(processId);
    }
}