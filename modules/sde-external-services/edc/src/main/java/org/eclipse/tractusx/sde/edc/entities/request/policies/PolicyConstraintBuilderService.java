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
import java.util.Comparator;
import java.util.List;

import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.edc.entities.request.policies.accesspolicy.AccessPolicyDTO;
import org.eclipse.tractusx.sde.policyhub.enums.ConstraintOperandIdEnum;
import org.eclipse.tractusx.sde.policyhub.enums.OperatorIdEnum;
import org.eclipse.tractusx.sde.policyhub.enums.PolicyTypeIdEnum;
import org.eclipse.tractusx.sde.policyhub.handler.IPolicyHubProxyService;
import org.eclipse.tractusx.sde.policyhub.model.request.Constraint;
import org.eclipse.tractusx.sde.policyhub.model.request.PolicyContentRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyConstraintBuilderService {

	private final IPolicyHubProxyService policyHubProxyService;

	public JsonNode getAccessPoliciesConstraints(PolicyModel policy) {
		return policyHubProxyService.getPolicyContent(
				mapPolicy(PolicyTypeIdEnum.ACCESS, ConstraintOperandIdEnum.OR, policy.getAccessPolicies()));
	}

	public JsonNode getUsagePoliciesConstraints(PolicyModel policy) {
		return policyHubProxyService.getPolicyContent(
				mapPolicy(PolicyTypeIdEnum.USAGE, ConstraintOperandIdEnum.AND, policy.getUsagePolicies()));
	}

	private PolicyContentRequest mapPolicy(PolicyTypeIdEnum policyType, ConstraintOperandIdEnum constraintOperandId, List<Policies> policies) {
		
		List<Constraint> constraintsList= new ArrayList<>();
		policies.forEach(policy->{
			List<String> valueList = policy.getValue();
			OperatorIdEnum operator = OperatorIdEnum.EQUALS;
			
			if (valueList.size() > 1) {
				operator = OperatorIdEnum.IN;
			} 
			
			for (String value : valueList) {
				constraintsList.add(Constraint.builder()
						.key(policy.getTechnicalKey())
						.operator(operator)
						.value(value).build());
			}
		});
		
		return PolicyContentRequest.builder()
				.policyType(policyType)
				.constraintOperand(constraintOperandId)
				.constraints(constraintsList)
				.build();
	}

	public ActionRequest getAccessPoliciesConstraints(List<String> bpnNumbers) {
		
		List<ConstraintRequest> constraints = new ArrayList<>();
		if (bpnNumbers != null && !bpnNumbers.isEmpty()) {
			AccessPolicyDTO accessPolicy = null;
			for (String bpnNumber : bpnNumbers) {
				accessPolicy = AccessPolicyDTO.builder().bpnNumber(bpnNumber).build();
				constraints.add(accessPolicy.toConstraint());
			}
		}
		
		ActionRequest action = ActionRequest.builder().build();
		action.addProperty("@type", "LogicalConstraint");
		action.addProperty("odrl:or", constraints);
		return action;
	}

	public ActionRequest getUsagePoliciesConstraints(List<Policies> usagePolicies) {
		List<ConstraintRequest> usageConstraintList = new ArrayList<>();
		
		if (usagePolicies != null && !usagePolicies.isEmpty()) {
			usagePolicies.forEach(policy -> usagePolicy(usageConstraintList, policy));
		}

		usageConstraintList.sort(Comparator.comparing(ConstraintRequest::getLeftOperand));

		if (!usageConstraintList.isEmpty()) {
			ActionRequest action = ActionRequest.builder().build();
			action.addProperty("@type", "LogicalConstraint");
			action.addProperty("odrl:and", usageConstraintList);
			return action;
		}
		return null;

	}

	private void usagePolicy(List<ConstraintRequest> policies, Policies policy) {
		
		String operator = "odrl:eq";
		
		ConstraintRequest request = ConstraintRequest.builder()
				.leftOperand(policy.getTechnicalKey())
				.operator(Operator.builder().id(operator).build())
				.rightOperand(policy.getValue())
				.build();
		
		if (request != null) {
			policies.add(request);
		}
	}

}
