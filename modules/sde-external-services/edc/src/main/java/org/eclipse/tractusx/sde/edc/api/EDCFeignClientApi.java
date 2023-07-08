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

package org.eclipse.tractusx.sde.edc.api;

import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyDefinitionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "EDCFeignClientApi", url = "${edc.hostname}${edc.managementpath:/data/v2}", configuration = EDCDataProviderConfiguration.class)
public interface EDCFeignClientApi {

	@GetMapping(path = "/assets/{id}")
	public ResponseEntity<Object> getAsset(@PathVariable("id") String assetId);

	@PostMapping("/assets")
	public String createAsset(@RequestBody AssetEntryRequest requestBody);

	@PostMapping("/policydefinitions")
	public String createPolicy(@RequestBody PolicyDefinitionRequest requestBody);

	@PostMapping("/contractdefinitions")
	public String createContractDefination(@RequestBody ContractDefinitionRequest requestBody);

	@DeleteMapping(path = "/contractdefinitions/{id}")
	public ResponseEntity<Object> deleteContractDefinition(@PathVariable("id") String contractdefinitionsId);

	@DeleteMapping(path = "/policydefinitions/{id}")
	public ResponseEntity<Object> deletePolicyDefinitions(@PathVariable("id") String policydefinitionsId);

	@DeleteMapping(path = "/assets/{id}")
	public ResponseEntity<Object> deleteAssets(@PathVariable("id") String assetsId);

}
