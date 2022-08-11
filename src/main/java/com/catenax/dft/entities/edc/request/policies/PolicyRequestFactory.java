/*
 * Copyright 2022 CatenaX
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.catenax.dft.entities.edc.request.policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.catenax.dft.usecases.common.UUIdGenerator;

@Service
public class PolicyRequestFactory {

	private static final String DATASPACECONNECTOR_LITERALEXPRESSION = "dataspaceconnector:literalexpression";

	public PolicyDefinitionRequest getPolicy(String shellId, String subModelId, List<String> bpnNumbers) {
		String assetId = shellId + "-" + subModelId;

		ArrayList<PermissionRequest> permissions = getPermissions(assetId, bpnNumbers);
		HashMap<String, String> extensibleProperties = new HashMap<>();
		HashMap<String, String> type = new HashMap<>();
		type.put("@policytype", "set");

		extensibleProperties.put("additionalProp1", "value1");

		return PolicyDefinitionRequest.builder()
				.uid(UUIdGenerator.getUrnUuid())
				.permissions(permissions)
				.prohibitions(new ArrayList<>())
				.obligations(new ArrayList<>())
				.extensibleProperties(extensibleProperties)
				.inheritsFrom(null)
				.assigner(null)
				.assignee(null)
				.target(assetId).type(type).build();
	}

	private ArrayList<PermissionRequest> getPermissions(String assetId, List<String> bpnNumbers) {
		ArrayList<PermissionRequest> permissions = new ArrayList<>();
		ActionRequest action = ActionRequest.builder()
				.type("USE")
				.includedIn(null)
				.constraint(null)
				.build();
		
		List<ConstraintRequest> constraints= new ArrayList<>();
		if(bpnNumbers !=null && !bpnNumbers.isEmpty()) {
			constraints.add(getBPNConstraints(bpnNumbers));
		}
		
		
		PermissionRequest permissionRequest = PermissionRequest.builder()
				.target(assetId)
				.action(action)
				.assignee(null)
				.assigner(null)
				.constraints(constraints)
				.duties(new ArrayList<>())
				.edcType("dataspaceconnector:permission")
				.build();
		permissions.add(permissionRequest);
		return permissions;
	}

	private ConstraintRequest getBPNConstraints(List<String> bpnNumbers) {
		Expression lExpression = Expression.builder()
				.edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
				.value("BusinessPartnerNumber")
				.build();

		String operator = "IN";
		Expression rExpression = null;
		if(bpnNumbers.size()==1) {
			operator = "EQ";
			rExpression = Expression.builder()
					.edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
					.value(bpnNumbers.get(0))
					.build();
		}
		else {
			rExpression = Expression.builder()
					.edcType(DATASPACECONNECTOR_LITERALEXPRESSION)
					.value(bpnNumbers)
					.build();
		}

		return ConstraintRequest.builder().edcType("AtomicConstraint")
				.leftExpression(lExpression)
				.rightExpression(rExpression)
				.operator(operator)
				.build();

	}
}
