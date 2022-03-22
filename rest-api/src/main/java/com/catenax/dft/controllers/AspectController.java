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

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.entities.digitalTwins.LookupRequest;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.external.DigitalTwinGateway;
import com.catenax.dft.usecases.aspects.GetAspectsUseCase;
import com.catenax.dft.usecases.csvHandler.aspects.RegisterDigitalTwinUseCase;
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
    private final RegisterDigitalTwinUseCase digitalTwinUseCase;
    public AspectController(GetAspectsUseCase useCase, RegisterDigitalTwinUseCase digitalTwinUseCase) {
        this.useCase = useCase;
        this.digitalTwinUseCase = digitalTwinUseCase;
    }

    @GetMapping(path = "/aspect")
    public ResponseEntity<Page<AspectEntity>> getAspects(@Param("page") Integer page, @Param("pageSize") Integer pageSize) {


        Aspect input= Aspect.builder()
                .localIdentifiersKey("PartInstanceID")
                .localIdentifiersValue("czxczxcxzc")
                .manufacturerPartId("34534dfgdfgsd")
                .customerPartId("234234sfdsdfgsdf")
                .build();
        digitalTwinUseCase.run(input);

        page = page == null ? 0 : page;
        pageSize = pageSize == null ? 10 : pageSize;
        return ok().body(useCase.fetchAllAspects(page, pageSize));
    }
}
