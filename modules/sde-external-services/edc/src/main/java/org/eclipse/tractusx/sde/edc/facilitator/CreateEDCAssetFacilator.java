/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.edc.facilitator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateEDCAssetFacilator extends AbstractEDCStepsHelper {

	private final EDCGateway edcGateway;
	private final ContractDefinitionRequestFactory contractFactory;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;

	public Map<String, String> createEDCAsset(AssetEntryRequest assetEntryRequest, List<String> bpns,
			List<Policies> usagePolicies) {

		Map<String, String> output = new HashMap<>();

		edcGateway.createAsset(assetEntryRequest);

		String assetId = "";

		JsonNode accessPolicyDefinitionRequest = policyConstraintBuilderService.getAccessPolicy(assetId, null);
		String accessPolicyUUId = accessPolicyDefinitionRequest.get("@id").asText();
		//edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);

		JsonNode usagePolicyDefinitionRequest = policyConstraintBuilderService.getUsagePolicy(assetId, null);
		String usagePolicyUUId = usagePolicyDefinitionRequest.get("@id").asText();
		//edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);

		ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(assetId,
				accessPolicyUUId, usagePolicyUUId);

		edcGateway.createContractDefinition(contractDefinitionRequest);

		output.put("accessPolicyId", accessPolicyUUId);
		output.put("usagePolicyId", usagePolicyUUId);
		output.put("contractDefinitionId", contractDefinitionRequest.getId());
		return output;

	}

}
