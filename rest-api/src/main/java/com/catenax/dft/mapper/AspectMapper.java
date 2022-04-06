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

import com.catenax.dft.entities.aspect.AspectResponse;
import com.catenax.dft.entities.aspect.LocalIdentifier;
import com.catenax.dft.entities.aspect.ManufacturingInformation;
import com.catenax.dft.entities.aspect.PartTypeInformation;
import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.entities.usecases.Aspect;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Mapper(componentModel = "spring")
public abstract class AspectMapper {
    @Value(value = "${manufacturerId}")
    private String manufacturerId;

    @Mapping(target = "rowNumber", ignore = true)
    public abstract Aspect mapFrom(AspectEntity aspect);

    public abstract AspectEntity mapFrom(Aspect aspect);

    public AspectResponse mapToResponse(AspectEntity entity) {

        if (entity==null) {
            return null;
        }

        ArrayList<LocalIdentifier> localIdentifiers = new ArrayList<>();
        localIdentifiers.add(new LocalIdentifier("PartInstanceID", entity.getPartInstanceId()));
        localIdentifiers.add(new LocalIdentifier("ManufacturerPartID", entity.getManufacturerPartId()));
        localIdentifiers.add(new LocalIdentifier("ManufacturerID", manufacturerId));
        if (entity.getOptionalIdentifierKey() != null && entity.getOptionalIdentifierValue() != null) {
            localIdentifiers.add(new LocalIdentifier(entity.getOptionalIdentifierKey().getPrettyName(), entity.getOptionalIdentifierValue()));
        }

        ManufacturingInformation manufacturingInformation = ManufacturingInformation.builder()
                .country(entity.getManufacturingCountry())
                .date(entity.getManufacturingDate())
                .build();

        PartTypeInformation partTypeInformation = PartTypeInformation.builder()
                .manufacturerPartID(entity.getManufacturerPartId())
                .customerPartId(entity.getCustomerPartId())
                .classification(entity.getClassification())
                .nameAtManufacturer(entity.getNameAtManufacturer())
                .nameAtCustomer(entity.getNameAtCustomer())
                .build();

        return AspectResponse.builder()
                .localIdentifiers(localIdentifiers)
                .manufacturingInformation(manufacturingInformation)
                .partTypeInformation(partTypeInformation)
                .catenaXId(entity.getUuid())
                .build();
    }
}
