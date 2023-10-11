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
import java.util.Optional;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyRequestFactory;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateEDCAssetFacilator extends AbstractEDCStepsHelper {

	private final EDCGateway edcGateway;
	private final PolicyRequestFactory policyFactory;
	private final ContractDefinitionRequestFactory contractFactory;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;

	public Map<String, String> createEDCAsset(AssetEntryRequest assetEntryRequest, List<String> bpns,
			Map<UsagePolicyEnum, UsagePolicies> usagePolicies) {

		Map<String, String> extensibleProperties = new HashMap<>();
		Map<String, String> output = new HashMap<>();

		edcGateway.createAsset(assetEntryRequest);

		String assetId = assetEntryRequest.getAsset().getId();

		ActionRequest accessAction = policyConstraintBuilderService.getAccessConstraints(bpns);

		prepareExtensionalCustomValue(extensibleProperties, usagePolicies);

		PolicyDefinitionRequest accessPolicyDefinitionRequest = policyFactory.getPolicy(assetId, accessAction,
				new HashMap<>());
		edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);
		String accessPolicyId = accessPolicyDefinitionRequest.getId();

		ActionRequest usageAction = policyConstraintBuilderService.getUsagePolicyConstraints(usagePolicies);
		PolicyDefinitionRequest usagePolicyDefinitionRequest = policyFactory.getPolicy(assetId, usageAction,
				extensibleProperties);
		edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);
		String usagePolicyId = usagePolicyDefinitionRequest.getId();

		ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(assetId,
				accessPolicyId, usagePolicyId);

		edcGateway.createContractDefinition(contractDefinitionRequest);

		output.put("accessPolicyId", accessPolicyId);
		output.put("usagePolicyId", usagePolicyId);
		output.put("contractDefinitionId", contractDefinitionRequest.getId());
		return output;

	}

	private void prepareExtensionalCustomValue(Map<String, String> extensibleProperties,
			Map<UsagePolicyEnum, UsagePolicies> usagePolicies) {
		if (!CollectionUtils.isEmpty(usagePolicies) && usagePolicies.containsKey(UsagePolicyEnum.CUSTOM)) {
			UsagePolicies customPolicy = usagePolicies.get(UsagePolicyEnum.CUSTOM);
			if (!Optional.ofNullable(customPolicy).isEmpty()
					&& PolicyAccessEnum.RESTRICTED.equals(customPolicy.getTypeOfAccess())) {
				extensibleProperties.put(UsagePolicyEnum.CUSTOM.name(), customPolicy.getValue());
			}
		}
	}

}
