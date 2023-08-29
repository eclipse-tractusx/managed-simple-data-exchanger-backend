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
import java.util.List;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.edc.entities.request.policies.accesspolicy.AccessPolicyDTO;
import org.eclipse.tractusx.sde.edc.entities.request.policies.usagepolicy.DurationPolicyDTO;
import org.eclipse.tractusx.sde.edc.entities.request.policies.usagepolicy.PurposePolicyDTO;
import org.eclipse.tractusx.sde.edc.entities.request.policies.usagepolicy.RolePolicyDTO;
import org.springframework.stereotype.Service;

@Service
public class PolicyConstraintBuilderService {

	public ActionRequest getAccessConstraints(List<String> bpnNumbers) {
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

	public ActionRequest getUsagePolicyConstraints(List<UsagePolicies> usagePolicies) {
		List<ConstraintRequest> usageConstraintList = new ArrayList<>();
		if (usagePolicies != null && !usagePolicies.isEmpty()) {
			usagePolicies.stream().forEach(policy -> usagePolicy(usageConstraintList, policy));
		}
		
		if (!usageConstraintList.isEmpty()) {
			ActionRequest action = ActionRequest.builder().build();
			action.addProperty("@type", "LogicalConstraint");
			action.addProperty("odrl:and", usageConstraintList);
			return action;
		}
		return null;

	}

	private void usagePolicy(List<ConstraintRequest> usageConstraintList, UsagePolicies policy) {
		ConstraintRequest request = null;

		switch (policy.getType()) {
		case DURATION:
			request = DurationPolicyDTO.fromUsagePolicy(policy).toConstraint();
			break;
		case PURPOSE:
			request = PurposePolicyDTO.fromUsagePolicy(policy).toConstraint();
			break;
		case ROLE:
			request = RolePolicyDTO.fromUsagePolicy(policy).toConstraint();
			break;
		default:
			break;
		}

		if (request != null) {
			usageConstraintList.add(request);
		}
	}
	
	public ConstraintRequest toTraceabilityConstraint() {
		String operator = "odrl:eq";
		return ConstraintRequest.builder()
				.leftOperand("FrameworkAgreement.traceability")
				.operator(Operator.builder().id(operator).build())
				.rightOperand("active")
				.build();
	}
}
