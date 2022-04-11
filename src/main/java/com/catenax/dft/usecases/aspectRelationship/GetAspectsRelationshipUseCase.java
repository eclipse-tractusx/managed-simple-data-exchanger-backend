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

package com.catenax.dft.usecases.aspectRelationship;

import com.catenax.dft.entities.aspectRelationship.AspectRelationshipResponse;
import com.catenax.dft.entities.database.AspectRelationshipEntity;
import com.catenax.dft.gateways.database.AspectRelationshipRepository;
import com.catenax.dft.mapper.AspectRelationshipMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAspectsRelationshipUseCase {

    private final AspectRelationshipRepository repository;
    private final AspectRelationshipMapper mapper;

    public GetAspectsRelationshipUseCase(AspectRelationshipRepository repository, AspectRelationshipMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AspectRelationshipResponse execute(String uuid) {
        List<AspectRelationshipEntity> entities = repository.findByParentCatenaXId(uuid);
        return mapper.mapToResponse(uuid, entities);
    }
}
