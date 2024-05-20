/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
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
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.edc.constants.EDCAssetConfigurableConstant;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyRequestFactory {

	private final EDCAssetConfigurableConstant edcAssetConfigurableConstant;
	
	public PolicyDefinitionRequest getPolicy(String assetId, ActionRequest action, String type) {

		List<PermissionRequest> permissions = getPermissions(assetId, action);

		PolicyRequest policyRequest = PolicyRequest.builder()
				.permissions(permissions)
				.profile(edcAssetConfigurableConstant.getCxPolicyPrefix()
						+ edcAssetConfigurableConstant.getCxPolicyProfile())
				.obligations(new ArrayList<>())
				.prohibitions(new ArrayList<>())
				.profile(edcAssetConfigurableConstant.getCxPolicyPrefix()
						+ edcAssetConfigurableConstant.getCxPolicyProfile())
				.target(Map.of("@id", assetId))
				.build();
		
		//Use submodel id to generate unique policy id for asset use policy type as prefix asset/usage
		String policyId = getGeneratedPolicyId(assetId, type);
				
		return PolicyDefinitionRequest.builder()
				.id(policyId)
				.policyRequest(policyRequest).build();
	}

	public JsonNode setPolicyIdAndGetObject(String assetId, JsonNode jsonNode, String type) {
		String policyId = getGeneratedPolicyId(assetId, type);
		return ((ObjectNode) jsonNode).put("@id", policyId);
	}
	
	private String getGeneratedPolicyId(String assetId, String type) {
		String submodelId = assetId;
		if (assetId.length() > 45) {
			submodelId = assetId.substring(46);
			submodelId = submodelId.replace("urn:uuid:", "");
		}
		return type + "-" + submodelId;
	}
	

	public List<PermissionRequest> getPermissions(String assetId, ActionRequest action) {

		ArrayList<PermissionRequest> permissions = new ArrayList<>();
		if (action != null) {
			PermissionRequest permissionRequest = PermissionRequest
					.builder().action(Map.of("odrl:type", "USE"))
//					.target(assetId)
					.constraint(action.getAction())
					.build();
			permissions.add(permissionRequest);
		}
		return permissions;
	}
}