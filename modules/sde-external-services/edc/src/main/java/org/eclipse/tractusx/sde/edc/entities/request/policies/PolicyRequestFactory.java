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
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class PolicyRequestFactory {

	private final EDCAssetConfigurableConstant edcAssetConfigurableConstant;
	
	public PolicyDefinitionRequest getPolicy(String policyId, String assetId, List<ActionRequest> action, String type) {

		List<PermissionRequest> permissions = getPermissions(assetId, action);

		Map<String,String> contextMap = Map.of(
				"@vocab", "https://w3id.org/edc/v0.0.1/ns/"
				);
		
		Map<String,String> contextMapPolicy= Map.of(
		        "cx-policy", "https://w3id.org/catenax/policy/",
				"tx", "https://w3id.org/tractusx/v0.0.1/ns/"
				);
		
		PolicyRequest policyRequest = PolicyRequest.builder()
				.context(List.of("http://www.w3.org/ns/odrl.jsonld", contextMapPolicy))
				.permissions(permissions)
				.profile(edcAssetConfigurableConstant.getCxPolicyPrefix()
						+ edcAssetConfigurableConstant.getCxPolicyProfile())
				.obligations(new ArrayList<>())
				.prohibitions(new ArrayList<>())
				.target(Map.of("@id", assetId))
				.build();
		
		//Use submodel id to generate unique policy id for asset use policy type as prefix asset/usage
		policyId = getGeneratedPolicyId(policyId, type);
				
		return PolicyDefinitionRequest.builder()
				.id(policyId)
				.context(contextMap)
				.policyRequest(policyRequest)
				.build();
	}

	@SneakyThrows
	public PolicyDefinitionRequest setPolicyIdAndGetObject(String assetId, JsonNode jsonNode, String type) {
		
		JsonNode contentPolicy= ((ObjectNode) jsonNode).get("content");
		
		((ObjectNode) contentPolicy).remove("@id");
		
		//Use submodel id to generate unique policy id for asset use policy type as prefix asset/usage
		String policyId = getGeneratedPolicyId(assetId, type);
				
		Map<String,String> contextMap = Map.of(
				"@vocab", "https://w3id.org/edc/v0.0.1/ns/"
				);
		
		return PolicyDefinitionRequest.builder()
				.id(policyId)
				.context(contextMap)
				.policyRequest(contentPolicy)
				.build();
	}
	
	private String getGeneratedPolicyId(String assetId, String type) {
		String submodelId = assetId;
		if (assetId.length() > 45) {
			submodelId = assetId.substring(46);
			submodelId = submodelId.replace("urn:uuid:", "");
		}
		return type + "-" + submodelId;
	}
	

	public List<PermissionRequest> getPermissions(String assetId, List<ActionRequest> actions) {

		ArrayList<PermissionRequest> permissions = new ArrayList<>();
		if (actions != null) {
			actions.forEach(action -> {
				PermissionRequest permissionRequest = PermissionRequest
						.builder()
						.action(LinkJsonLDId.builder().id("odrl:use").build())
//						.target(assetId)
						.constraint(action.getAction())
						.build();
				permissions.add(permissionRequest);
			});
		}
		return permissions;
	}
}