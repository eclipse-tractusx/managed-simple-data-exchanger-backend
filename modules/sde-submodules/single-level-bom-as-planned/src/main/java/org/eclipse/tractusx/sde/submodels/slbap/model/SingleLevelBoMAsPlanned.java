/********************************************************************************
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
package org.eclipse.tractusx.sde.submodels.slbap.model;

import org.eclipse.tractusx.sde.common.entities.CommonPropEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SingleLevelBoMAsPlanned  extends CommonPropEntity{

	@JsonProperty(value = "parent_uuid")
	private String parentUuid;
	
	@JsonProperty(value = "parent_manufacturer_part_id")
	private String parentManufacturerPartId;
	
	@JsonProperty(value = "uuid")
	private String childUuid;
	
	@JsonProperty(value = "manufacturer_part_id")
	private String childManufacturerPartId;

	@JsonProperty(value = "customer_part_id")
	private String customerPartId;
	
	@JsonProperty(value = "quantity_number")
	private String quantityNumber;
	
	@JsonProperty(value = "measurement_unit_lexical_value")
	private String measurementUnitLexicalValue;
	
	@JsonProperty(value = "datatype_uri")
	private String datatypeURI;
	
	@JsonProperty(value = "created_on")
	private String createdOn;

	@JsonProperty(value = "last_modified_on")
	private String lastModifiedOn;
	
}