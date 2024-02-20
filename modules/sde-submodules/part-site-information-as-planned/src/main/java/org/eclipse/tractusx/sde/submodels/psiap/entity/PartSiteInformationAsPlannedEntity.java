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
package org.eclipse.tractusx.sde.submodels.psiap.entity;

import org.eclipse.tractusx.sde.common.entities.CommonPropEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(name = "Part_site_information_as_planned")
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class PartSiteInformationAsPlannedEntity extends CommonPropEntity {

	@Id
	@Column(name = "uuid")
	private String uuid;

	@Column(name = "manufacturer_part_id")
	private String manufacturerPartId;

	@Column(name = "name_at_manufacturer")
	private String nameAtManufacturer;

	@Column(name = "catenax_site_id")
	private String catenaXSiteId;

	@Column(name = "function")
	private String function;

	@Column(name = "function_valid_from")
	private String functionValidFrom;

	@Column(name = "function_valid_until")
	private String functionValidUntil;

}