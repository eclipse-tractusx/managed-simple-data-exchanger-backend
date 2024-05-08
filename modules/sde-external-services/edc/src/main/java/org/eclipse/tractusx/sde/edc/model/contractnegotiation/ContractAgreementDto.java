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

package org.eclipse.tractusx.sde.edc.model.contractnegotiation;

import org.eclipse.tractusx.sde.edc.constants.EDCAssetConstant;
import org.eclipse.tractusx.sde.edc.model.policies.PolicyDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractAgreementDto {

	@JsonProperty("@id")
	private String id;

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "providerId")
	private String providerAgentId;

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "consumerId")
	private String consumerAgentId;

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "contractSigningDate")
	private long contractSigningDate;

	private long contractStartDate;
	private long contractEndDate;

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "assetId")
	private String assetId;

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "policy")
	private PolicyDefinition policy;

}
