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

package com.catenax.dft.usecases.csvhandler.aspectrelationship;

import org.springframework.stereotype.Service;

import com.catenax.dft.entities.database.AspectRelationshipEntity;
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.gateways.database.AspectRelationshipRepository;
import com.catenax.dft.mapper.AspectRelationshipMapper;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StoreAspectRelationshipCsvHandlerUseCase
        extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private final AspectRelationshipRepository repository;
    private final AspectRelationshipMapper mapper;

    public StoreAspectRelationshipCsvHandlerUseCase(AspectRelationshipRepository aspectRelationshipRepository,
                                                    AspectRelationshipMapper mapper) {
        super(null);
        this.repository = aspectRelationshipRepository;
        this.mapper = mapper;
    }

    protected AspectRelationship executeUseCase(AspectRelationship input, String processId) {
        AspectRelationshipEntity entity = mapper.mapFrom(input);
        repository.save(entity);

        return input;
    }
}