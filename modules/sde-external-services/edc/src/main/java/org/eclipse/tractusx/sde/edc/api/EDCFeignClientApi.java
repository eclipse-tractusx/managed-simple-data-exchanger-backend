/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import org.eclipse.tractusx.sde.edc.entities.request.businesspartnergroup.BusinessPartnerGroupRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@FeignClient(value = "EDCFeignClientApi", url = "${edc.hostname}${edc.managementpath:/data}", configuration = EDCDataProviderConfiguration.class)
public interface EDCFeignClientApi {

	//Assets
	@GetMapping(path = "${edc.managementpath.apiversion.asset:/v3}/assets/{id}")
	public ResponseEntity<Object> getAsset(@PathVariable("id") String assetId);

	@PostMapping("${edc.managementpath.apiversion.asset:/v3}/assets")
	public String createAsset(@RequestBody AssetEntryRequest requestBody);
	
	@PutMapping("${edc.managementpath.apiversion.asset:/v3}/assets")
	public void updateAsset(@RequestBody AssetEntryRequest requestBody);
	
	
	@PostMapping("${edc.managementpath.apiversion.asset:/v3}/assets/request")
	public JsonNode getAssetByType(@RequestBody ObjectNode requestBody);
	
	@DeleteMapping(path = "${edc.managementpath.apiversion.asset:/v3}/assets/{id}")
	public ResponseEntity<Object> deleteAssets(@PathVariable("id") String assetsId);
	
	
	//Policy & Contract
	@GetMapping("${edc.managementpath.apiversion:/v2}/policydefinitions/{id}")
	public JsonNode getPolicy(@PathVariable("id") String policyId);
	
	@PostMapping("${edc.managementpath.apiversion:/v2}/policydefinitions")
	public JsonNode createPolicy(@RequestBody JsonNode requestBody);

	@PutMapping("${edc.managementpath.apiversion:/v2}/policydefinitions/{id}")
	public void updatePolicy(@PathVariable("id") String policyUUId, @RequestBody JsonNode requestBody);

	
	//Contract defination
	@PostMapping("${edc.managementpath.apiversion:/v2}/contractdefinitions")
	public String createContractDefination(@RequestBody ContractDefinitionRequest requestBody);
	
	@PutMapping("${edc.managementpath.apiversion:/v2}/contractdefinitions")
	public void updateContractDefination(@RequestBody ContractDefinitionRequest requestBody);
	
	@GetMapping("${edc.managementpath.apiversion:/v2}/contractdefinitions/{id}")
	public JsonNode getContractDefination(@PathVariable("id") String id);


	@DeleteMapping(path = "${edc.managementpath.apiversion:/v2}/contractdefinitions/{id}")
	public ResponseEntity<Object> deleteContractDefinition(@PathVariable("id") String contractdefinitionsId);

	@DeleteMapping(path = "${edc.managementpath.apiversion:/v2}/policydefinitions/{id}")
	public ResponseEntity<Object> deletePolicyDefinitions(@PathVariable("id") String policydefinitionsId);

	
	//Business Partner Group
	@GetMapping("/business-partner-groups/{bpn}")
	public JsonNode getBusinessPartnerGroups(@PathVariable("bpn") String bpn);
	
	@PutMapping("/business-partner-groups")
	public void updateBusinessPartnerGroups(@RequestBody BusinessPartnerGroupRequest requestBody);
	
	@PostMapping("/business-partner-groups")
	public void createBusinessPartnerGroups(@RequestBody BusinessPartnerGroupRequest requestBody);
	
	@DeleteMapping("/business-partner-groups/{bpn}")
	public void deleteBusinessPartnerGroups(@PathVariable("bpn") String bpn);

}