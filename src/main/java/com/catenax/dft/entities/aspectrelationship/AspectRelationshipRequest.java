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

package com.catenax.dft.entities.aspectrelationship;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class AspectRelationshipRequest {
    @JsonIgnore
    private int rowNumber;
    @JsonIgnore
    private String processId;

    @JsonProperty(value = "child_uuid")
    private String childUuid;

    @JsonProperty(value = "parent_uuid")
    private String parentUuid;

    @JsonProperty(value = "parent_part_instance_id")
    private String parentPartInstanceId;

    @JsonProperty(value = "parent_manufacturer_part_id")
    private String parentManufacturerPartId;

    @JsonProperty(value = "parent_optional_identifier_key")
    private String parentOptionalIdentifierKey;

    @JsonProperty(value = "parent_optional_identifier_value")
    private String parentOptionalIdentifierValue;

    @JsonProperty(value = "part_instance_id")
    private String childPartInstanceId;

    @JsonProperty(value = "manufacturer_part_id")
    private String childManufacturerPartId;

    @JsonProperty(value = "child_optional_identifier_key")
    private String childOptionalIdentifierKey;

    @JsonProperty(value = "child_optional_identifier_value")
    private String childOptionalIdentifierValue;

    @JsonProperty(value = "lifecycle_context")
    private String lifecycleContext;

    @JsonProperty(value = "quantity_number")
    private String quantityNumber;

    @JsonProperty(value = "measurement_unit_lexical_value")
    private String measurementUnitLexicalValue;

    @JsonProperty(value = "datatype_uri")
    private String dataTypeUri;

    @JsonProperty(value = "assembled_on")
    private String assembledOn;

}
