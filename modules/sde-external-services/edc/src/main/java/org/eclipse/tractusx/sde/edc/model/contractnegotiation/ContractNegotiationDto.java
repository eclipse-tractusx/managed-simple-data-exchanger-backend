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
import org.eclipse.tractusx.sde.edc.enums.Type;

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
public class ContractNegotiationDto {

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "contractAgreementId")
	private String contractAgreementId; // is null until state == FINALIZED

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "counterPartyAddress")
	private String counterPartyAddress;

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "errorDetail")
	private String errorDetail;

	@JsonProperty("@id")
	private String id;

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "protocol")
	@Builder.Default
	private String protocol = "dataspace-protocol-http";

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "state")
	private String state;

	@JsonProperty(EDCAssetConstant.ASSET_PREFIX + "type")
	private Type type;

	private long createdAt;
	private long updatedAt;

}
