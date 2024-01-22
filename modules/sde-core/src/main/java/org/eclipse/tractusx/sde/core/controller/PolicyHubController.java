/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.core.controller;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.eclipse.tractusx.sde.policyhub.handler.IPolicyHubProxyService;
import org.eclipse.tractusx.sde.policyhub.model.request.PolicyContentRequest;
import org.eclipse.tractusx.sde.policyhub.model.response.PolicyTypeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/policy-hub")
public class PolicyHubController {
	
	@Autowired
	private IPolicyHubProxyService policyHubProxyService;
	
	
	@GetMapping(value = "/policy-attributes")
	@PreAuthorize("hasPermission('','policyhub_view_policy_attributes')")
	public ResponseEntity<List<String>> policyAttributes() throws Exception {
		
		log.info("Request received : /policy-hub/policy-attributes");
		List<String> policyAttributesResponse = policyHubProxyService.getPolicyAttributes();
		return ok().body(policyAttributesResponse);
	}
	
	
	@GetMapping(value = "/policy-types")
	@PreAuthorize("hasPermission('','policyhub_view_policy_types')")
	public ResponseEntity<List<PolicyTypeResponse>> policyTypes(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "useCase", required = false) String useCase) throws Exception {
		
		log.info("Request received : /policy-hub/policy-types");
		List<PolicyTypeResponse> policyTypesResponse = policyHubProxyService.getPolicyTypes(type, useCase);
		return ok().body(policyTypesResponse);
	}
	
	@GetMapping(value = "/policy-content")
	@PreAuthorize("hasPermission('','policyhub_view_policy_content')")
	public ResponseEntity<JsonNode> policyContent(@RequestParam(value = "useCase", required = false) String useCase,
			@RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "credential", required = true) String credential,
			@RequestParam(value = "operatorId", required = true) String operatorId,
			@RequestParam(value = "value", required = false) String value) throws Exception {
		
		log.info("Request received : /policy-hub/policy-content");
		JsonNode policyResponse = policyHubProxyService.getPolicyContent(useCase, type, credential, operatorId, value);
		return ok().body(policyResponse);
	}
	
	@PostMapping(value = "/policy-content")
	@PreAuthorize("hasPermission('','policyhub_policy_content')")
	public ResponseEntity<JsonNode> policyContent(@RequestBody PolicyContentRequest policyContentRequest) throws Exception {
		
		log.info("Request received : /policy-hub/policy-content");
		JsonNode policyResponse = policyHubProxyService.getPolicyContent(policyContentRequest);
		return ok().body(policyResponse);
	}
	

}
