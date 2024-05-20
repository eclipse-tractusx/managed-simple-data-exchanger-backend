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

package org.eclipse.tractusx.sde.edc.entities.request.policies;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.configuration.properties.SDEConfigurationProperties;
import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.mapper.JsonObjectMapper;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConfigurableConstant;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyConstraintBuilderService {

	private final PolicyRequestFactory policyRequestFactory;

	private final JsonObjectMapper jsonobjectMapper;

	private final EDCAssetConfigurableConstant edcAssetConfigurableConstant;

	private final SDEConfigurationProperties sdeConfigurationProperties;

//	private final IPolicyHubProxyService policyHubProxyService;

//	public JsonNode getAccessPolicy(String assetId, PolicyModel policy) {
//		
//		return policyRequestFactory.setPolicyIdAndGetObject(assetId,
//				policyHubProxyService.getPolicyContent(
//						mapPolicy(PolicyTypeIdEnum.ACCESS, ConstraintOperandIdEnum.OR, policy.getAccessPolicies(), "a")).get("content"),
//				"a");
//	}
//
//	public JsonNode getUsagePolicy(String assetId, PolicyModel policy) {
//		
//		return policyRequestFactory.setPolicyIdAndGetObject(assetId,
//				policyHubProxyService.getPolicyContent(
//						mapPolicy(PolicyTypeIdEnum.USAGE, ConstraintOperandIdEnum.AND, policy.getUsagePolicies(), "u")).get("content"),
//				"u");
//	}

//	private PolicyContentRequest mapPolicy(PolicyTypeIdEnum policyType, ConstraintOperandIdEnum constraintOperandId,
//			List<Policies> policies, String type) {
//
//		List<Constraint> constraintsList = new ArrayList<>();
//		policies.forEach(policy -> {
//			
//			List<String> valueList = policy.getValue();
//
//			if (type.equals("a"))
//				valueList = getAndOwnerBPNIfNotExist(policy, valueList);
//			
//			OperatorIdEnum operator = OperatorIdEnum.EQUALS;
//
//			if (valueList.size() > 1) {
//				operator = OperatorIdEnum.IN;
//			}
//
//			for (String value : valueList) {
//				if (StringUtils.isNotBlank(value)) {
//					constraintsList.add(
//							Constraint.builder()
//							.key(policy.getTechnicalKey())
//							.operator(operator)
//							.value(value)
//							.build());
//				}
//			}
//		});
//
//		return PolicyContentRequest.builder()
//				.policyType(policyType)
//				.constraintOperand(constraintOperandId)
//				.constraints(constraintsList)
//				.build();
//	}

	public JsonNode getAccessPolicy(String assetId, PolicyModel policy) {
		return jsonobjectMapper.objectToJsonNode(policyRequestFactory.getPolicy(assetId,
				getPoliciesConstraints(policy.getAccessPolicies(), "odrl:or", "a"), "a"));
	}

	public JsonNode getUsagePolicy(String assetId, PolicyModel policy) {
		return jsonobjectMapper.objectToJsonNode(policyRequestFactory.getPolicy(assetId,
				getPoliciesConstraints(policy.getUsagePolicies(), "odrl:and", "u"), "u"));
	}

	public ActionRequest getUsagePoliciesConstraints(List<Policies> policies) {
		return getPoliciesConstraints(policies, "odrl:and", "u");
	}

	public ActionRequest getPoliciesConstraints(List<Policies> usagePolicies, String operator, String type) {
		List<ConstraintRequest> constraintList = new ArrayList<>();

		if (usagePolicies != null && !usagePolicies.isEmpty()) {
			usagePolicies.forEach(policy -> preparePolicyConstraint(constraintList, policy, type));
		}

		constraintList.sort(Comparator.comparing(ConstraintRequest::getLeftOperand));

		if (!constraintList.isEmpty()) {
			ActionRequest action = ActionRequest.builder().build();
			action.addProperty("@type", "LogicalConstraint");
			action.addProperty(operator, constraintList);
			return action;
		}
		return null;

	}

	private void preparePolicyConstraint(List<ConstraintRequest> policies, Policies policy, String type) {

		String operator = "odrl:eq";

		List<String> values = policy.getValue();

		if (type.equals("a"))
			values = getAndOwnerBPNIfNotExist(policy, values);

		for (String value : values) {
			if (StringUtils.isNotBlank(value)) {

				String policyPrefix = "";

				if (!policy.getTechnicalKey().startsWith(edcAssetConfigurableConstant.getCxPolicyPrefix())) {
					policyPrefix = edcAssetConfigurableConstant.getCxPolicyPrefix();
				}

				ConstraintRequest request = ConstraintRequest.builder()
						.leftOperand(policyPrefix + policy.getTechnicalKey())
						.operator(Operator.builder().id(operator).build()).rightOperand(value).build();
				policies.add(request);
			}
		}
	}

	private List<String> getAndOwnerBPNIfNotExist(Policies policy, List<String> values) {

		if (policy.getTechnicalKey().equals(edcAssetConfigurableConstant.getBpnNumberTechnicalKey())
				&& !values.isEmpty() && !values.contains(sdeConfigurationProperties.getManufacturerId())
				&& (values.size() == 1 && !values.get(0).equals(""))) {
			List<String> temp = new ArrayList<>();
			values.stream().forEach(temp::add);
			temp.add(sdeConfigurationProperties.getManufacturerId());
			values = temp;

		}

		return values;

	}

}
