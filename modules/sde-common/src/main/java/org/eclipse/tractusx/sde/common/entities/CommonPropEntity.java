/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.common.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonPropEntity {

	@Transient
	@JsonProperty(value = "row_number")
	private Integer rowNumber;

	@Column(name = "process_id")
	@JsonProperty(value = "process_id")
	private String processId;

	@Column(name = "shell_id")
	@JsonProperty(value = "shell_id")
	private String shellId;

	@Column(name = "sub_model_id")
	@JsonProperty(value = "sub_model_id")
	private String subModelId;

	@Column(name = "usage_policy_id")
	@JsonProperty(value = "usage_policy_id")
	private String usagePolicyId;

	@Column(name = "asset_id")
	@JsonProperty(value = "asset_id")
	private String assetId;

	@Column(name = "access_policy_id")
	@JsonProperty(value = "access_policy_id")
	private String accessPolicyId;

	@Column(name = "contract_defination_id")
	@JsonProperty(value = "contract_defination_id")
	private String contractDefinationId;

	@Column(name = "deleted")
	@JsonProperty(value = "deleted")
	private String deleted;

	@Column(name = "updated")
	@JsonProperty(value = "updated")
	private String updated;

}
