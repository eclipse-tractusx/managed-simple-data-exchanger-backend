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

package org.eclipse.tractusx.sde.edc.gateways.external;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.edc.api.EDCFeignClientApi;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.businesspartnergroup.BusinessPartnerGroupRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.exceptions.EDCGatewayException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EDCGateway {

	private final EDCFeignClientApi edcFeignClientApi;
	
	public boolean assetExistsLookup(String id) {
		try {
			edcFeignClientApi.getAsset(id);
		} catch (FeignException e) {
			if (e.status() == HttpStatus.NOT_FOUND.value()) {
				return false;
			}
			throw e;
		}
		return true;
	}
	
	public boolean assetExistsLookupBasedOnType(ObjectNode requestBody) {
		try {
			JsonNode result = edcFeignClientApi.getAssetByType(requestBody);
			if (result.isArray() && result.isEmpty())
				return false;
		} catch (FeignException e) {
			if (e.status() == HttpStatus.NOT_FOUND.value()) {
				return false;
			}
			throw e;
		}
		return true;
	}
	
	public JsonNode assetExistsLookupBasedOnTypeGetAsAsset(ObjectNode requestBody) {
		try {
			return edcFeignClientApi.getAssetByType(requestBody);
		} catch (FeignException e) {
			if (e.status() == HttpStatus.NOT_FOUND.value()) {
				return null;
			}
			throw e;
		}
	}

	public String createAsset(AssetEntryRequest request) {
		try {
			return edcFeignClientApi.createAsset(request);
		} catch (FeignException e) {
			if (e.status() == HttpStatus.CONFLICT.value()) {
				throw new EDCGatewayException("Asset already exists");
			}
			throw new EDCGatewayException(e.getMessage());
		}
	}
	
	public void updateAsset(AssetEntryRequest request) {
		try {
			edcFeignClientApi.updateAsset(request);
		} catch (FeignException e) {
			if (e.status() == HttpStatus.CONFLICT.value()) {
				throw new EDCGatewayException("Asset already exists");
			}
			throw new EDCGatewayException(e.getMessage());
		}
	}

	
	public boolean policyExistsLookup(String policyId) {
		try {
			edcFeignClientApi.getPolicy(policyId);
		} catch (FeignException e) {
			if (e.status() == HttpStatus.NOT_FOUND.value()) {
				return false;
			}
			throw e;
		}
		return true;
	}
	
	@SneakyThrows
	public JsonNode createPolicyDefinition(JsonNode request) {
		try {
			return edcFeignClientApi.createPolicy(request);
		} catch (FeignException e) {
			throw new EDCGatewayException(e.getMessage());
		}
	}
	
	@SneakyThrows
	public void updatePolicyDefinition(String policyUUId, JsonNode request) {
		try {
			edcFeignClientApi.updatePolicy(policyUUId, request);
		} catch (FeignException e) {
			throw new EDCGatewayException(e.getMessage());
		}
	}

	
	public boolean contractDefinitionExistsLookup(String contractDefinitionId) {
		try {
			edcFeignClientApi.getContractDefination(contractDefinitionId);
		} catch (FeignException e) {
			if (e.status() == HttpStatus.NOT_FOUND.value()) {
				return false;
			}
			throw e;
		}
		return true;
	}
	
	public String createContractDefinition(ContractDefinitionRequest request) {
		try {
			return edcFeignClientApi.createContractDefination(request);
		} catch (FeignException e) {
			throw new EDCGatewayException(e.getMessage());
		}
	}
	
	public void updateContractDefinition(ContractDefinitionRequest request) {
		try {
			edcFeignClientApi.updateContractDefination(request);
		} catch (FeignException e) {
			throw new EDCGatewayException(e.getMessage());
		}
	}
	
	public void addBPNintoPCFBusinessPartnerGroup(String bpnNumber, String groupName) {
		
		try {
			if (StringUtils.isNotBlank(bpnNumber)) {
				edcFeignClientApi.getBusinessPartnerGroups(bpnNumber);
			}else {
				log.info("BPN found empty not adding in business partner group {}", groupName);
			}
		} catch (FeignException e) {
			log.info("BPN not exist in group so adding business partner group {}, {}", groupName, e.getMessage());
			if (e.status() == HttpStatus.NOT_FOUND.value()) {
				edcFeignClientApi.createBusinessPartnerGroups(
						BusinessPartnerGroupRequest.builder().id(bpnNumber).groups(List.of(groupName)).build());
				
			}
		}
	}

	public void deleteBPNfromPCFBusinessPartnerGroup(String bpnNumber, String groupName) {
		try {
			if (StringUtils.isNotBlank(bpnNumber)) {
				edcFeignClientApi.deleteBusinessPartnerGroups(bpnNumber);
			}else {
				log.info("BPN found empty not going to delete from business partner group {}", groupName);
			}
		} catch (FeignException e) {
			log.error("Unable to perform delete bpn {} from business partner group {}, {}", bpnNumber, groupName, e.getMessage());
		}
	}
}