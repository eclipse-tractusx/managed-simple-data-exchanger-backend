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

package com.catenax.sde.entities.usecases;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import com.catenax.sde.edc.entities.UsagePolicy;
import com.catenax.sde.validators.DateValidation;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AspectRelationship {

    private String shellId;
    private String subModelId;
    private int rowNumber;
    private String processId;
    private String childUuid;
    private String parentUuid;
    private List<String> bpnNumbers;
    private String typeOfAccess;
    private List<UsagePolicy> usagePolicies;
    private String usagePolicyId;
    private String assetId; 
    private String accessPolicyId;
    private String contractDefinationId;
    private String deleted;
    

    @NotBlank(message = "parent_part_instance_id cannot be empty")
    private String parentPartInstanceId;

    @NotBlank(message = "parent_manufacturer_part_Id cannot be empty")
    private String parentManufacturerPartId;

    private String parentOptionalIdentifierKey;
    private String parentOptionalIdentifierValue;

    @NotBlank(message = "part_instance_id cannot be empty")
    private String childPartInstanceId;

    @NotBlank(message = "manufacturer_part_Id cannot be empty")
    private String childManufacturerPartId;

    private String childOptionalIdentifierKey;
    private String childOptionalIdentifierValue;

    @NotBlank(message = "lifecycle_context cannot be empty")
    private String lifecycleContext;

    @Positive(message = "quantity_number cannot be non number")
    private String quantityNumber;

    @NotBlank(message = "measurement_unit_lexical_value cannot be empty")
    private String measurementUnitLexicalValue;


    @NotBlank(message = "datatype_URI cannot be empty")
    private String dataTypeUri;

    @DateValidation
    @NotBlank(message = "assembled_on cannot be empty")
    private String assembledOn;

    public boolean hasOptionalParentIdentifier() {
        boolean hasKey = this.getParentOptionalIdentifierKey() != null && !this.getParentOptionalIdentifierKey().isBlank();
        boolean hasValue = this.getParentOptionalIdentifierValue() != null && !this.getParentOptionalIdentifierValue().isBlank();

        return hasKey && hasValue;
    }
}

