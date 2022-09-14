/********************************************************************************
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