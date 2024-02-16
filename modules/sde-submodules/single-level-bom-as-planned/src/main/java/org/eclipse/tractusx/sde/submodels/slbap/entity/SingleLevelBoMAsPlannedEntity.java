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
package org.eclipse.tractusx.sde.submodels.slbap.entity;

import org.eclipse.tractusx.sde.common.entities.CommonPropEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(name = "single_level_bom_as_planned")
@Entity
@Data
@IdClass(SingleLevelBoMAsPlannedPrimaryKey.class)
@EqualsAndHashCode(callSuper = false)
public class SingleLevelBoMAsPlannedEntity extends CommonPropEntity {

	@Id
	@Column(name = "parent_uuid")
	private String parentCatenaXId;

	@Column(name = "parent_manufacturer_part_id")
	private String parentManufacturerPartId;

	@Id
	@Column(name = "uuid")
	private String childCatenaXId;

	@Column(name = "manufacturer_part_id")
	private String childManufacturerPartId;

	@Column(name = "customer_part_id")
	private String customerPartId;

	@Column(name = "quantity_number")
	private Double quantityNumber;

	@Column(name = "measurement_unit_lexical_value")
	private String measurementUnitLexicalValue;

	@Column(name = "datatype_uri")
	private String datatypeURI;

	@Column(name = "created_on")
	private String createdOn;

	@Column(name = "last_modified_on")
	private String lastModifiedOn;

}
