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

package org.eclipse.tractusx.sde.digitaltwins.gateways.external;

import java.net.URI;
import java.util.List;

import org.eclipse.tractusx.sde.common.model.KeycloakJWTTokenResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "DigitalTwinsFeignClient", url = "${digital-twins.hostname:default}${digital-twins.api:/api/v3.0}", configuration = DigitalTwinsFeignClientConfiguration.class)
public interface DigitalTwinsFeignClient {

	@PostMapping
	KeycloakJWTTokenResponse readAuthToken(URI url, @RequestBody MultiValueMap<String, Object> body);

	@PostMapping(path = "/shell-descriptors")
	ResponseEntity<ShellDescriptorResponse> createShellDescriptor(@RequestBody ShellDescriptorRequest request);

	@GetMapping(path = "/shell-descriptors/{aasIdentifier}")
	ResponseEntity<ShellDescriptorResponse> getShellDescriptorByShellId(@PathVariable("aasIdentifier") String shellId,
			@RequestHeader("Edc-Bpn") String edcBpn);

	@DeleteMapping(path = "/shell-descriptors/{aasIdentifier}")
	ResponseEntity<Void> deleteShell(@PathVariable("assetIds") String shellId);

	@PostMapping(path = "/shell-descriptors/{aasIdentifier}/submodel-descriptors")
	ResponseEntity<String> createSubModel(@PathVariable("aasIdentifier") String shellId,
			@RequestBody CreateSubModelRequest request);

	@GetMapping(path = "/shell-descriptors/{aasIdentifier}/submodel-descriptors")
	ResponseEntity<SubModelListResponse> getSubModels(@PathVariable("aasIdentifier") String shellId);

	@DeleteMapping(path = "/shell-descriptors/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
	ResponseEntity<Object> deleteSubmodelfromShellById(@PathVariable("aasIdentifier") String shellId,
			@PathVariable("submodelIdentifier") String submodelIdentifier);

	@GetMapping(path = "/lookup/shells")
	ResponseEntity<ShellLookupResponse> shellLookup(@RequestParam String assetIds,
			@RequestHeader("Edc-Bpn") String edcBpn);

	@PostMapping(path = "/lookup/shells/{assetIds}")
	ResponseEntity<List<Object>> createShellSpecificAttributes(@PathVariable("assetIds") String shellId,
			@RequestHeader("Edc-Bpn") String edcBpn, @RequestBody List<Object> specificAssetIds);

	@DeleteMapping(path = "/lookup/shells/{assetIds}")
	ResponseEntity<Void> deleteShellSpecificAttributes(@PathVariable("assetIds") String shellId,
			@RequestHeader("Edc-Bpn") String edcBpn);

}