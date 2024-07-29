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

	private static final String U = "u";

	private static final String A = "a";

	private final PolicyRequestFactory policyRequestFactory;

	private final JsonObjectMapper jsonobjectMapper;

	private final EDCAssetConfigurableConstant edcAssetConfigurableConstant;

	private final SDEConfigurationProperties sdeConfigurationProperties;

//	private final IPolicyHubProxyService policyHubProxyService;
//
//	public JsonNode getAccessPolicy(String assetId, PolicyModel policy) {
//		
//		return jsonobjectMapper.objectToJsonNode(policyRequestFactory.setPolicyIdAndGetObject(assetId,
//				policyHubProxyService.getPolicyContent(
//						mapPolicy(PolicyTypeIdEnum.ACCESS, ConstraintOperandIdEnum.OR, policy.getAccessPolicies(), "a")),
//				"a"));
//	}
//
//	public JsonNode getUsagePolicy(String assetId, PolicyModel policy) {
//		
//		return jsonobjectMapper.objectToJsonNode(policyRequestFactory.setPolicyIdAndGetObject(assetId,
//				policyHubProxyService.getPolicyContent(
//						mapPolicy(PolicyTypeIdEnum.USAGE, ConstraintOperandIdEnum.AND, policy.getUsagePolicies(), "u")),
//				"u"));
//	}
//
//	private PolicyContentRequest mapPolicy(PolicyTypeIdEnum policyType, ConstraintOperandIdEnum constraintOperandId,
//			List<Policies> policies, String type) {
//
//		List<Constraint> constraintsList = new ArrayList<>();
//		policies.forEach(policy -> {
//			
//			List<String> valueList = policy.getValue();
//
//			//if (type.equals("a"))
//			//	valueList = getAndOwnerBPNIfNotExist(policy, valueList);
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

	public JsonNode getAccessPolicy(String policyId, String assetId, PolicyModel policy) {
		return jsonobjectMapper.objectToJsonNode(
				policyRequestFactory.getPolicy(policyId, assetId, getPoliciesConstraints(policy.getAccessPolicies(), A), A));
	}

	public JsonNode getUsagePolicy( String policyId, String assetId, PolicyModel policy) {
		return jsonobjectMapper.objectToJsonNode(
				policyRequestFactory.getPolicy(policyId, assetId, getPoliciesConstraints(policy.getUsagePolicies(), U), U));
	}

	public List<ActionRequest> getUsagePoliciesConstraints(List<Policies> policies) {
		return getPoliciesConstraints(policies, U);
	}

	public List<ActionRequest> getPoliciesConstraints(List<Policies> policies, String type) {

		List<ConstraintRequest> constraintList = new ArrayList<>();

		List<ConstraintRequest> bpnConstraintList = new ArrayList<>();

		if (policies != null && !policies.isEmpty()) {
			policies.forEach(policy -> {
				if (type.equals(A)
						&& policy.getTechnicalKey().equals(edcAssetConfigurableConstant.getBpnNumberTechnicalKey())) {
					preparePolicyConstraint(bpnConstraintList, policy, getAndOwnerBPNIfNotExist(policy.getValue()));
				} else {
					preparePolicyConstraint(constraintList, policy, policy.getValue());
				}
			});
		}

		List<ActionRequest> actionList = new ArrayList<>();
		if (!constraintList.isEmpty()) {
			actionList.add(prepareActionRequest("odrl:and", constraintList));
		}

		if (!bpnConstraintList.isEmpty()) {
			actionList.add(prepareActionRequest("odrl:or", bpnConstraintList));
		}

		return actionList;

	}

	private ActionRequest prepareActionRequest(String operator, List<ConstraintRequest> constraintList) {
		constraintList.sort(Comparator.comparing(a -> a.getLeftOperand().getId()));
		ActionRequest action = ActionRequest.builder().build();
		action.addProperty("@type", "LogicalConstraint");
		action.addProperty(operator, constraintList);
		return action;
	}

	private void preparePolicyConstraint(List<ConstraintRequest> policies, Policies policy, List<String> values) {

		String operator = "odrl:eq";

		for (String value : values) {

			if (StringUtils.isNotBlank(value)) {

				String policyPrefix = "";

				if (!policy.getTechnicalKey().startsWith(edcAssetConfigurableConstant.getCxPolicyPrefix())
						&& !policy.getTechnicalKey().contains(":")) {
					policyPrefix = edcAssetConfigurableConstant.getCxPolicyPrefix();
				}

				if (StringUtils.isNotBlank(policy.getOperator())) {
					operator = policy.getOperator();
				}

				ConstraintRequest request = ConstraintRequest.builder()
						.leftOperand(LinkJsonLDId.builder().id(policyPrefix + policy.getTechnicalKey()).build())
						.operator(LinkJsonLDId.builder().id(operator).build()).rightOperand(value).build();
				policies.add(request);
			}
		}
	}

	private List<String> getAndOwnerBPNIfNotExist(List<String> values) {

		if (!values.isEmpty() && !values.contains(sdeConfigurationProperties.getManufacturerId())
				&& (values.size() == 1 && !values.get(0).equals(""))) {
			List<String> temp = new ArrayList<>();
			values.stream().forEach(temp::add);
			temp.add(sdeConfigurationProperties.getManufacturerId());
			values = temp;
		}

		return values;

	}

}
