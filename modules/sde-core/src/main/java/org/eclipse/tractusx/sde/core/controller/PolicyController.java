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

import java.util.Map;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.common.validators.ValidatePolicyTemplate;
import org.eclipse.tractusx.sde.core.policy.service.PolicyService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("policy")
@PreAuthorize("hasPermission('','policy_management')")
@Validated
public class PolicyController {

	private final PolicyService policyService;

	@PostMapping
	public PolicyModel savePolicy(@RequestBody @Valid @ValidatePolicyTemplate PolicyModel request) {
		return policyService.savePolicy(request);
	}

	@PutMapping("/{uuid}")
	public PolicyModel updatePolicy(@PathVariable String uuid,
			@RequestBody @Valid @ValidatePolicyTemplate PolicyModel request) {
		return policyService.updatePolicy(uuid, request);
	}

	@GetMapping("/{uuid}")
	public PolicyModel getPolicy(@PathVariable String uuid) {
		return policyService.getPolicy(uuid);
	}

	@GetMapping("/is-policy-name-valid")
	public Map<String, Boolean> isPolicyNameValid(@RequestParam String policyName) {
		return Map.of("msg", policyService.isPolicyNameValid("", policyName));
	}
	
	@GetMapping
	public PagingResponse getAllPolicies(@Param("page") Integer page, @Param("pageSize") Integer pageSize) {
		page = page == null ? 0 : page;
		pageSize = pageSize == null ? 10 : pageSize;
		return policyService.getAllPolicies(page, pageSize);
	}

	@DeleteMapping("/{uuid}")
	public ResponseEntity<Object> deletePolicy(@PathVariable String uuid) {
		policyService.deletePolicy(uuid);
		return ResponseEntity.noContent().build();
	}

}