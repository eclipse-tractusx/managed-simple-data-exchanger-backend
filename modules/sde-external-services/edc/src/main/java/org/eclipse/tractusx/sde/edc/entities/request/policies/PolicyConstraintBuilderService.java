/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.mapper.JsonObjectMapper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyConstraintBuilderService {

	private final PolicyRequestFactory policyRequestFactory;

	private final JsonObjectMapper jsonobjectMapper;

	// private final IPolicyHubProxyService policyHubProxyService;

//	public JsonNode getAccessPoliciesConstraints(PolicyModel policy) {
//		return policyHubProxyService.getPolicyContent(
//				mapPolicy(PolicyTypeIdEnum.ACCESS, ConstraintOperandIdEnum.OR, policy.getAccessPolicies()));
//	}
//
//	public JsonNode getUsagePoliciesConstraints(PolicyModel policy) {
//		return policyHubProxyService.getPolicyContent(
//				mapPolicy(PolicyTypeIdEnum.USAGE, ConstraintOperandIdEnum.AND, policy.getUsagePolicies()));
//	}

//	private PolicyContentRequest mapPolicy(PolicyTypeIdEnum policyType, ConstraintOperandIdEnum constraintOperandId,
//			List<Policies> policies) {
//
//		List<Constraint> constraintsList = new ArrayList<>();
//		policies.forEach(policy -> {
//			List<String> valueList = policy.getValue();
//			OperatorIdEnum operator = OperatorIdEnum.EQUALS;
//
//			if (valueList.size() > 1) {
//				operator = OperatorIdEnum.IN;
//			}
//
//			for (String value : valueList) {
//				constraintsList.add(
//						Constraint.builder().key(policy.getTechnicalKey()).operator(operator).value(value).build());
//			}
//		});
//
//		return PolicyContentRequest.builder().policyType(policyType).constraintOperand(constraintOperandId)
//				.constraints(constraintsList).build();
//	}

	public JsonNode getAccessPolicy(String assetId, PolicyModel policy) {
		return jsonobjectMapper.objectToJsonNode(policyRequestFactory.getPolicy(assetId,
				getPoliciesConstraints(policy.getAccessPolicies(), "odrl:or"), Collections.emptyMap()));
	}

	public JsonNode getUsagePolicy(String assetId, PolicyModel policy) {
		return jsonobjectMapper.objectToJsonNode(policyRequestFactory.getPolicy(assetId,
				getPoliciesConstraints(policy.getUsagePolicies(), "odrl:and"), Collections.emptyMap()));
	}

	public ActionRequest getUsagePoliciesConstraints(List<Policies> policies) {
		return getPoliciesConstraints(policies, "odrl:and");
	}

	public ActionRequest getPoliciesConstraints(List<Policies> usagePolicies, String operator) {
		List<ConstraintRequest> constraintList = new ArrayList<>();

		if (usagePolicies != null && !usagePolicies.isEmpty()) {
			usagePolicies.forEach(policy -> preparePolicyConstraint(constraintList, policy));
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

	private void preparePolicyConstraint(List<ConstraintRequest> policies, Policies policy) {

		String operator = "odrl:eq";

		ConstraintRequest request = ConstraintRequest.builder().leftOperand(policy.getTechnicalKey())
				.operator(Operator.builder().id(operator).build()).rightOperand(policy.getValue()).build();

		if (request != null) {
			policies.add(request);
		}
	}

}
