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
package org.eclipse.tractusx.sde.policyhub.api;

import java.util.List;

import org.eclipse.tractusx.sde.policyhub.model.request.PolicyContentRequest;
import org.eclipse.tractusx.sde.policyhub.model.response.PolicyTypeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(value = "IPolicyHubExternalServiceApi", url = "${policy.hub.hostname}", configuration = PolicyHubExternalServiceApi.class)
public interface IPolicyHubExternalServiceApi {
	
	@GetMapping(path = "/policy-hub/policy-attributes")
	List<String> getPolicyAttributes();
	
	@GetMapping(path = "/policy-hub/policy-types")
	List<PolicyTypeResponse> getPolicyTypes(@RequestParam("type") String type, @RequestParam("useCase") String useCase);
	
	@GetMapping(path = "/policy-hub/policy-content")
	JsonNode getPolicyContent(@RequestParam("useCase") String useCase, @RequestParam("type") String type, @RequestParam("credential") String credential, @RequestParam("operatorId") String operatorId, @RequestParam("value") String value);

	@PostMapping(path = "/policy-hub/policy-content")
	JsonNode getPolicyContent(@RequestBody PolicyContentRequest policyContentRequest);

}
