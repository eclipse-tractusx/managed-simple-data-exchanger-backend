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

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubmodelDescriptionListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "DigitalTwinsFeignClient", url = "${digital-twins.hostname}")
public interface DigitalTwinsFeignClient {

	@DeleteMapping(path = "/registry/shell-descriptors/{aasIdentifier}/submodel-descriptors/{submodelIdentifier}")
	ResponseEntity<Object> deleteSubmodelfromShellById(@PathVariable("aasIdentifier") String shellId,
			@PathVariable("submodelIdentifier") String submodelIdentifier,
			@RequestHeader Map<String, String> requestHeader);

	@PostMapping(path = "/registry/shell-descriptors/fetch")
	SubmodelDescriptionListResponse getShellDescriptorsWithSubmodelDetails(@RequestHeader Map<String, String> requestHeader,
			@RequestBody List<String> body);
	

}
