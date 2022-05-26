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

package com.catenax.dft.entities.usecases;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Builder
@Data
public class AspectRelationship {

    private String shellId;
    private String subModelId;
    private int rowNumber;
    private String processId;
    private String childUuid;
    private String parentUuid;

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

    @Positive(message = "quantity_number cannot be empty")
    private String quantityNumber;

    @NotBlank(message = "measurement_unit_lexical_value cannot be empty")
    private String measurementUnitLexicalValue;
  


    @NotBlank(message = "datatype_URI cannot be empty")
    private String dataTypeUri;

    @NotBlank(message = "assembled_on cannot be empty")
    private String assembledOn;

    public boolean hasOptionalParentIdentifier() {
        boolean hasKey = this.getParentOptionalIdentifierKey() != null && !this.getParentOptionalIdentifierKey().isBlank();
        boolean hasValue = this.getParentOptionalIdentifierValue() != null && !this.getParentOptionalIdentifierValue().isBlank();

        return hasKey && hasValue;
    }
}

