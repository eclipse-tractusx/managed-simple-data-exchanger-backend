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

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.catenax.dft.entities.aspectrelationship.AspectRelationshipRequest;
import com.catenax.dft.entities.aspectrelationship.AspectRelationshipResponse;
import com.catenax.dft.entities.aspectrelationship.ChildPart;
import com.catenax.dft.entities.aspectrelationship.MeasurementUnit;
import com.catenax.dft.entities.aspectrelationship.Quantity;
import com.catenax.dft.entities.database.AspectRelationshipEntity;
import com.catenax.dft.entities.usecases.AspectRelationship;

@Mapper(componentModel = "spring")
public abstract class AspectRelationshipMapper {

    @Mapping(source = "parentUuid", target = "parentCatenaXId")
    @Mapping(source = "childUuid", target = "childCatenaXId")
    public abstract AspectRelationshipEntity mapFrom(AspectRelationship aspectRelationShip);

    @Mapping(target = "subModelId", ignore = true)
    @Mapping(target = "shellId", ignore = true)
    public abstract AspectRelationship mapFrom(AspectRelationshipRequest aspectRelationship);

    public AspectRelationshipResponse mapToResponse(String parentCatenaXUuid, List<AspectRelationshipEntity> aspectRelationships) {

        if (aspectRelationships == null || aspectRelationships.isEmpty()) {
            return null;
        }

        List<ChildPart> childParts = aspectRelationships.stream().map(this::toChildPart).toList();
        return AspectRelationshipResponse.builder()
                .catenaXId(parentCatenaXUuid)
                .childParts(childParts)
                .build();

    }

    private ChildPart toChildPart(AspectRelationshipEntity entity) {
        Quantity quantity = Quantity.builder()
                .quantityNumber(entity.getQuantityNumber())
                .measurementUnit(new MeasurementUnit(entity.getMeasurementUnitLexicalValue(), entity.getDataTypeUri()))
                .build();

        return ChildPart.builder()
                .lifecycleContext(entity.getLifecycleContext())
                .assembledOn(entity.getAssembledOn())
                .childCatenaXId(entity.getChildCatenaXId())
                .quantity(quantity)
                .build();
    }
}
