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

package com.catenax.dft.entities.aspect;

import java.util.List;

import com.catenax.dft.entities.UsagePolicyRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AspectRequest {
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
    private List<UsagePolicyRequest> usagePolicies;

    @JsonProperty(value = "part_instance_id")
    private String partInstanceId;

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