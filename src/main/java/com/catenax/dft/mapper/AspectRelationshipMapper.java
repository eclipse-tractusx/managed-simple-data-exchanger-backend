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

package com.catenax.dft.mapper;

import com.catenax.dft.entities.aspectRelationship.AspectRelationshipResponse;
import com.catenax.dft.entities.aspectRelationship.ChildPart;
import com.catenax.dft.entities.aspectRelationship.MeasurementUnit;
import com.catenax.dft.entities.aspectRelationship.Quantity;
import com.catenax.dft.entities.database.AspectRelationshipEntity;
import com.catenax.dft.entities.usecases.AspectRelationship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Mapper(componentModel = "spring")
public abstract class AspectRelationshipMapper {

    @Mapping(source = "parentUuid", target = "parentCatenaXId")
    @Mapping(source = "childUuid", target = "childCatenaXId")
    public abstract AspectRelationshipEntity mapFrom(AspectRelationship aspectRelationShip);

    public AspectRelationshipResponse mapToResponse(String parentCatenaXUuid, List<AspectRelationshipEntity> aspectRelationships) {

        if (aspectRelationships == null || aspectRelationships.isEmpty()) {
            return null;
        }

        List<ChildPart> childParts = aspectRelationships.stream().map(this::toChildPart).collect(Collectors.toList());
        return AspectRelationshipResponse.builder()
                .catenaXId(parentCatenaXUuid)
                .childParts(childParts)
                .build();

    }

    private ChildPart toChildPart(AspectRelationshipEntity entity) {
        Quantity quantity = Quantity.builder()
                .quantityNumber(Double.parseDouble(entity.getQuantityNumber()))
                .measurementUnit(new MeasurementUnit(entity.getMeasurementUnitLexicalValue(), "urn:bamm:io.openmanufacturing:meta-model:1.0.0#curie"))
                .build();

        return ChildPart.builder()
                .lifecycleContext(entity.getLifecycleContext())
                .assembledOn(entity.getAssembledOn())
                .childCatenaXId(entity.getChildCatenaXId())
                .quantity(quantity)
                .build();
    }
}
