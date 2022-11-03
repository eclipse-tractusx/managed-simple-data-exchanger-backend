/********************************************************************************
 * Copyright (c) 2022 T-Systems International Gmbh
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

package com.catenax.sde.entities.batch;

import java.util.List;

import com.catenax.sde.edc.entities.UsagePolicy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BatchRequest {
    @JsonIgnore
    private int rowNumber;
    private String uuid;
    @JsonIgnore
    private String processId;
    @JsonIgnore
    private List<String> bpnNumbers;
    @JsonIgnore
    private String typeOfAccess;
    @JsonIgnore
    private List<UsagePolicy> usagePolicies;

    @JsonProperty(value = "batch_id")
    private String batchId;

    @JsonProperty(value = "manufacturing_date")
    private String manufacturingDate;

    @JsonProperty(value = "manufacturing_country")
    private String manufacturingCountry;

    @JsonProperty(value = "manufacturer_part_id")
    private String manufacturerPartId;

    @JsonProperty(value = "customer_part_id")
    private String customerPartId;

    private String classification;

    @JsonProperty(value = "name_at_manufacturer")
    private String nameAtManufacturer;

    @JsonProperty(value = "name_at_customer")
    private String nameAtCustomer;

    @JsonProperty(value = "optional_identifier_key")
    private String optionalIdentifierKey;

    @JsonProperty(value = "optional_identifier_value")
    private String optionalIdentifierValue;
}