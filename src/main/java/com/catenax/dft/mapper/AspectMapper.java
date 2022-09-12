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

package com.catenax.dft.mapper;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;

import com.catenax.dft.entities.aspect.AspectRequest;
import com.catenax.dft.entities.aspect.AspectResponse;
import com.catenax.dft.entities.aspect.LocalIdentifier;
import com.catenax.dft.entities.aspect.ManufacturingInformation;
import com.catenax.dft.entities.aspect.PartTypeInformation;
import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.enums.OptionalIdentifierKeyEnum;

@Mapper(componentModel = "spring")
public abstract class AspectMapper {
    @Value(value = "${manufacturerId}")
    private String manufacturerId;

    @Mapping(target = "rowNumber", ignore = true)
    @Mapping(target = "subModelId", ignore = true)
    public abstract Aspect mapFrom(AspectEntity aspect);

    @Mapping(source = "optionalIdentifierKey",
            target = "optionalIdentifierKey",
            qualifiedByName = "prettyName")
    public abstract AspectEntity mapFrom(Aspect aspect);

    @Mapping(target = "subModelId", ignore = true)
    @Mapping(target = "shellId", ignore = true)
    public abstract Aspect mapFrom(AspectRequest aspect);

    public AspectResponse mapToResponse(AspectEntity entity) {

        if (entity == null) {
            return null;
        }

        ArrayList<LocalIdentifier> localIdentifiers = new ArrayList<>();
        localIdentifiers.add(new LocalIdentifier("PartInstanceID", entity.getPartInstanceId()));
        localIdentifiers.add(new LocalIdentifier("ManufacturerPartID", entity.getManufacturerPartId()));
        localIdentifiers.add(new LocalIdentifier("ManufacturerID", manufacturerId));
        if (entity.getOptionalIdentifierKey() != null && entity.getOptionalIdentifierValue() != null) {
            localIdentifiers.add(new LocalIdentifier(entity.getOptionalIdentifierKey(), entity.getOptionalIdentifierValue()));
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

    @Named("prettyName")
    String getPrettyName(String optionalIdentifierKey) {
        return optionalIdentifierKey == null ? null : Stream.of(OptionalIdentifierKeyEnum.values())
                .filter(v -> v.getPrettyName().equalsIgnoreCase(optionalIdentifierKey)).findFirst().get().getPrettyName();
    }
}
