/********************************************************************************
 * Copyright (c) 2022,2024 T-Systems International GmbH
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.edc.mapper;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyRequest;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiations;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class ContractMapper {

	private final ContractPolicyMapper contractPolicyMapper;

	@SneakyThrows
	public ContractNegotiations prepareContractNegotiations(String providerProtocolUrl, String offerId, String assetId,
			String provider, List<ActionRequest> action) {

		PolicyRequest policy = contractPolicyMapper.preparePolicy(assetId, action);
		policy.setId(offerId);
		policy.setAssigner(Map.of("@id", provider));
		return ContractNegotiations.builder()
				.connectorAddress(providerProtocolUrl)
				.protocol("dataspace-protocol-http")
				.policy(policy)
				.build();

	}

}