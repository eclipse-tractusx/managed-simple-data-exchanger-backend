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
import java.util.Map;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
		String accessPolicyUUId = UUID.randomUUID().toString();
		String usagePolicyUUId = UUID.randomUUID().toString();

		JsonNode accessPolicyDefinitionRequest = policyConstraintBuilderService.getAccessPoliciesConstraints(policy);
		((ObjectNode) accessPolicyDefinitionRequest.get("content")).put("@id", accessPolicyUUId);
		edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);

		JsonNode usagePolicyDefinitionRequest = policyConstraintBuilderService.getUsagePoliciesConstraints(policy);
		((ObjectNode) usagePolicyDefinitionRequest.get("content")).put("@id", usagePolicyUUId);
		edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);

		ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(assetId,
				accessPolicyUUId, usagePolicyUUId);

		edcGateway.createContractDefinition(contractDefinitionRequest);

		output.put("accessPolicyId", accessPolicyUUId);
		output.put("usagePolicyId", usagePolicyUUId);
		output.put("contractDefinitionId", contractDefinitionRequest.getId());
		return output;

	}	


}
