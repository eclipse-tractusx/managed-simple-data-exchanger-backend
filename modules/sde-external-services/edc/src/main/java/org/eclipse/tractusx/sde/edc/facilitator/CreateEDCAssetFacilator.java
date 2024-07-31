/********************************************************************************
 * Copyright (c) 2023, 2024 T-Systems International GmbH
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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
import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.SubmoduleCommonColumnsConstant;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
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

	public Map<String, String> createEDCAsset(AssetEntryRequest assetEntryRequest, PolicyModel policy) {

		Map<String, String> output = new HashMap<>();

		edcGateway.createAsset(assetEntryRequest);

		String assetId = assetEntryRequest.getId();

		JsonNode accessPolicyDefinitionRequest = policyConstraintBuilderService.getAccessPolicy(assetId, assetId, policy);
		String accessPolicyUUId = accessPolicyDefinitionRequest.get("@id").asText();
		edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);

		JsonNode usagePolicyDefinitionRequest = policyConstraintBuilderService.getUsagePolicy(assetId ,assetId, policy);
		String usagePolicyUUId = usagePolicyDefinitionRequest.get("@id").asText();
		edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);

		ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(assetId,
				assetId, accessPolicyUUId, usagePolicyUUId);

		edcGateway.createContractDefinition(contractDefinitionRequest);

		output.put(SubmoduleCommonColumnsConstant.ASSET_ID, assetId);
		output.put(SubmoduleCommonColumnsConstant.ACCESS_POLICY_ID, accessPolicyUUId);
		output.put(SubmoduleCommonColumnsConstant.USAGE_POLICY_ID, usagePolicyUUId);
		output.put(SubmoduleCommonColumnsConstant.CONTRACT_DEFINATION_ID, contractDefinitionRequest.getId());
		return output;

	}

	public Map<String, String> updateEDCAsset(AssetEntryRequest assetEntryRequest, PolicyModel policy) {

		Map<String, String> output = new HashMap<>();

		edcGateway.updateAsset(assetEntryRequest);

		String assetId = assetEntryRequest.getId();

		JsonNode accessPolicyDefinitionRequest = policyConstraintBuilderService.getAccessPolicy(assetId, assetId, policy);
		String accessPolicyUUId = accessPolicyDefinitionRequest.get("@id").asText();
		edcGateway.updatePolicyDefinition(accessPolicyUUId, accessPolicyDefinitionRequest);

		JsonNode usagePolicyDefinitionRequest = policyConstraintBuilderService.getUsagePolicy(assetId, assetId, policy);
		String usagePolicyUUId = usagePolicyDefinitionRequest.get("@id").asText();
		edcGateway.updatePolicyDefinition(usagePolicyUUId, usagePolicyDefinitionRequest);

		ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(assetId,
				assetId, accessPolicyUUId, usagePolicyUUId);

		edcGateway.updateContractDefinition(contractDefinitionRequest);

		output.put(SubmoduleCommonColumnsConstant.ASSET_ID, assetId);
		output.put(SubmoduleCommonColumnsConstant.ACCESS_POLICY_ID, accessPolicyUUId);
		output.put(SubmoduleCommonColumnsConstant.USAGE_POLICY_ID, usagePolicyUUId);
		output.put(SubmoduleCommonColumnsConstant.CONTRACT_DEFINATION_ID, contractDefinitionRequest.getId());
		return output;
	}
}
