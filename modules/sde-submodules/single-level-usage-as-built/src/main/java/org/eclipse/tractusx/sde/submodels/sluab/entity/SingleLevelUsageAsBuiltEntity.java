/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.submodels.sluab.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "single_level_usage_as_built")
@Data
@IdClass(SingleLevelUsageAsBuiltPrimaryKey.class)
public class SingleLevelUsageAsBuiltEntity implements Serializable {
	
    @Column(name = "process_id")
    private String processId;
    @Id
    @Column(name = "parent_uuid")
    private String parentCatenaXId;
    @Column(name = "parent_part_instance_id")
	private String parentPartInstanceId;
    @Column(name = "parent_manufacturer_part_id")
	private String parentManufacturerPartId;
    @Column(name = "parent_optional_identifier_key")
	private String parentOptionalIdentifierKey;
    @Column(name = "parent_optional_identifier_value")
	private String parentOptionalIdentifierValue;
    @Id
    @Column(name = "uuid")
    private String childCatenaXId;
    @Column(name = "part_instance_id")
	private String childPartInstanceId;
    @Column(name = "manufacturer_part_id")
	private String childManufacturerPartId;
    @Column(name = "optional_identifier_key")
	private String childOptionalIdentifierKey;
    @Column(name = "optional_identifier_value")
	private String childOptionalIdentifierValue;
    @Column(name = "quantity_number")
    private Double quantityNumber;
    @Column(name = "measurement_unit")
    private String measurementUnit;
    @Column(name = "created_on")
    private String createdOn;
    @Column(name = "last_modified_on")
    private String lastModifiedOn;
    @Column(name = "shell_id")
    private String shellId;
    @Column(name = "sub_model_id")
    private String subModelId;
    @Column(name = "usage_policy_id")
    private String usagePolicyId;
    @Column(name = "contract_defination_id")
    private String contractDefinationId;
    @Column(name = "asset_id")
    private String assetId;
    @Column(name = "access_policy_id")
    private String accessPolicyId;
    @Column(name = "deleted")
    private String deleted;
    @Column(name = "updated")
    private String updated;

}
