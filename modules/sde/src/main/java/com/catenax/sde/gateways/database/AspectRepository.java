/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
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

package com.catenax.sde.gateways.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.catenax.sde.entities.database.AspectEntity;
import com.catenax.sde.enums.OptionalIdentifierKeyEnum;

public interface AspectRepository extends CrudRepository<AspectEntity, String> {

    AspectEntity findByPartInstanceIdAndManufacturerPartIdAndOptionalIdentifierKeyAndOptionalIdentifierValue(String partInstanceId,
                                                                                                             String manufacturerPartId,
                                                                                                             OptionalIdentifierKeyEnum optionalIdentifierKey,
                                                                                                             String optionalIdentifierValue);

    AspectEntity findByPartInstanceIdAndManufacturerPartIdAndOptionalIdentifierKeyIsNullAndOptionalIdentifierValueIsNull(String partInstanceId,
                                                                                                                         String manufacturerId);

    default AspectEntity findByIdentifiers(String partInstanceId,
                                           String manufacturerPartId,
                                           String optionalIdentifierKey,
                                           String optionalIdentifierValue) {
        return findByPartInstanceIdAndManufacturerPartIdAndOptionalIdentifierKeyAndOptionalIdentifierValue(partInstanceId,
                manufacturerPartId,
                OptionalIdentifierKeyEnum.valueOf(optionalIdentifierKey),
                optionalIdentifierValue);
    }

    default AspectEntity findByIdentifiers(String partInstanceId, String manufacturerId) {
        return findByPartInstanceIdAndManufacturerPartIdAndOptionalIdentifierKeyIsNullAndOptionalIdentifierValueIsNull(partInstanceId,
                manufacturerId);
    }

    AspectEntity findByUuid(String uuid);
    
    List<AspectEntity> findByProcessId(String processId);
}