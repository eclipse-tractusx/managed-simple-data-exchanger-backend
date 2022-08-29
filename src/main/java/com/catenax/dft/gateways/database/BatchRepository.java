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

import com.catenax.dft.entities.database.BatchEntity;
import com.catenax.dft.enums.OptionalIdentifierKeyEnum;

public interface BatchRepository extends CrudRepository<BatchEntity, String> {

	BatchEntity findByBatchIdAndManufacturerPartIdAndOptionalIdentifierKeyAndOptionalIdentifierValue(String batchId,
                                                                                                             String manufacturerPartId,
                                                                                                             OptionalIdentifierKeyEnum optionalIdentifierKey,
                                                                                                             String optionalIdentifierValue);

	BatchEntity findByBatchIdAndManufacturerPartIdAndOptionalIdentifierKeyIsNullAndOptionalIdentifierValueIsNull(String batchId,
                                                                                                                         String manufacturerId);

    default BatchEntity findByIdentifiers(String batchId,
                                           String manufacturerPartId,
                                           String optionalIdentifierKey,
                                           String optionalIdentifierValue) {
        return findByBatchIdAndManufacturerPartIdAndOptionalIdentifierKeyAndOptionalIdentifierValue(batchId,
                manufacturerPartId,
                OptionalIdentifierKeyEnum.valueOf(optionalIdentifierKey),
                optionalIdentifierValue);
    }

    default BatchEntity findByIdentifiers(String batchId, String manufacturerId) {
        return findByBatchIdAndManufacturerPartIdAndOptionalIdentifierKeyIsNullAndOptionalIdentifierValueIsNull(batchId,
                manufacturerId);
    }

    BatchEntity findByUuid(String uuid);
}