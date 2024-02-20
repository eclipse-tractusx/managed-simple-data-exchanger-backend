/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.submodels.apr.model;

import org.eclipse.tractusx.sde.common.entities.CommonPropEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AspectRelationship extends CommonPropEntity {

	private String oldSubmodelIdforUpdateCase;

	@JsonProperty(value = "uuid")
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

	@JsonProperty(value = "manufacturer_id")
	private String childManufacturerId;
	
	@JsonProperty(value = "optional_identifier_key")
	private String childOptionalIdentifierKey;

	@JsonProperty(value = "optional_identifier_value")
	private String childOptionalIdentifierValue;

	@JsonProperty(value = "lifecycle_context")
	private String lifecycleContext;

	@JsonProperty(value = "quantity_number")
	private String quantityNumber;

	@JsonProperty(value = "measurement_unit")
	private String measurementUnit;

	@JsonProperty(value = "created_on")
	private String createdOn;

	@JsonProperty(value = "last_modified_on")
	private String lastModifiedOn;
	
	public boolean hasOptionalParentIdentifier() {
		boolean hasKey = this.getParentOptionalIdentifierKey() != null
				&& !this.getParentOptionalIdentifierKey().isBlank();
		boolean hasValue = this.getParentOptionalIdentifierValue() != null
				&& !this.getParentOptionalIdentifierValue().isBlank();

		return hasKey && hasValue;
	}
	
	public boolean hasOptionalChildIdentifier() {
		boolean hasKey = this.getChildOptionalIdentifierKey() != null
				&& !this.getChildOptionalIdentifierKey().isBlank();
		boolean hasValue = this.getChildOptionalIdentifierValue() != null
				&& !this.getChildOptionalIdentifierValue().isBlank();

		return hasKey && hasValue;
	}
}