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

package org.eclipse.tractusx.sde.submodels.batch.model;

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
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Batch extends CommonPropEntity {

	@JsonProperty(value = "uuid")
	private String uuid;

	@JsonProperty(value = "batch_id")
	private String batchId;

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

	@JsonProperty(value = "classification")
	private String classification;

	@JsonProperty(value = "name_at_manufacturer")
	private String nameAtManufacturer;

	@JsonProperty(value = "name_at_customer")
	private String nameAtCustomer;

}