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

package com.catenax.dft.gateways.database;

import org.springframework.data.repository.CrudRepository;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.enums.OptionalIdentifierKeyEnum;

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
}