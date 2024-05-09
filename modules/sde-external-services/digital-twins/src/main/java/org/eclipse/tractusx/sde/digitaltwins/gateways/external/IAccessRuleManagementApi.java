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
package org.eclipse.tractusx.sde.digitaltwins.gateways.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(value = "IAccessRuleManagementApi", url = "${digital-twins.hostname:default}", configuration = DigitalTwinsFeignClientConfiguration.class)
public interface IAccessRuleManagementApi {

	@GetMapping(path = "${digital-twins.registry.uri:/api/v3}/access-controls/rules")
	public JsonNode getAccessControlsRules(@RequestHeader("Edc-Bpn") String edcBpn);

	@PostMapping(path = "${digital-twins.registry.uri:/api/v3}/access-controls/rules")
	public JsonNode createAccessControlsRule(@RequestHeader("Edc-Bpn") String edcBpn, @RequestBody JsonNode request);

	@GetMapping(path = "${digital-twins.registry.uri:/api/v3}/access-controls/rules/{ruleId}")
	public JsonNode getAccessControlsRuleById(@PathVariable("ruleId") String ruleId,
			@RequestHeader("Edc-Bpn") String edcBpn);

	@PutMapping(path = "${digital-twins.registry.uri:/api/v3}/access-controls/rules/{ruleId}")
	public JsonNode updateAccessControlsRule(@PathVariable("ruleId") String ruleId,
			@RequestHeader("Edc-Bpn") String edcBpn, @RequestBody JsonNode request);

	@DeleteMapping(path = "${digital-twins.registry.uri:/api/v3}/access-controls/rules/{ruleId}")
	public void deleteAccessControlsRule(@PathVariable("ruleId") String ruleId,
			@RequestHeader("Edc-Bpn") String edcBpn);

}