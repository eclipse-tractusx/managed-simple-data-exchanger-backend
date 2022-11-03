/********************************************************************************
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

package com.catenax.dft.entities.usecases;

import java.util.List;

import javax.validation.constraints.NotBlank;

import com.catenax.dft.entities.UsagePolicy;
import com.catenax.dft.validators.SubmodelValidation;
import com.catenax.dft.validators.DateValidation;
import com.catenax.dft.validators.OptionalIdentifierKeyValidation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@SubmodelValidation
public class Batch {

    private String shellId;
    private String subModelId;
    private int rowNumber;
    private String uuid;
    private String processId;
    
    private List<String> bpnNumbers;
    private String typeOfAccess;
    private List<UsagePolicy> usagePolicies;

    @NotBlank(message = "batch_id cannot be empty")
    private String batchId;

    @DateValidation
    @NotBlank(message = "manufacturing_date cannot be empty")
    private String manufacturingDate;

    private String manufacturingCountry;

    @NotBlank(message = "manufacturing_part_id cannot be empty")
    private String manufacturerPartId;

    private String customerPartId;

    @NotBlank(message = "classification cannot be empty")
    private String classification;

    @NotBlank(message = "name_at_manufacturer cannot be empty")
    private String nameAtManufacturer;

    private String nameAtCustomer;

    @OptionalIdentifierKeyValidation
    private String optionalIdentifierKey;
    private String optionalIdentifierValue;


    public boolean hasOptionalIdentifier() {
        boolean hasKey = this.getOptionalIdentifierKey() != null && !this.getOptionalIdentifierKey().isBlank();
        boolean hasValue = this.getOptionalIdentifierValue() != null && !this.getOptionalIdentifierValue().isBlank();

        return hasKey && hasValue;
    }
}
