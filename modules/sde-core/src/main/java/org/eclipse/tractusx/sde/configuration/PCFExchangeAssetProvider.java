/********************************************************************************
 * Copyright (c) 2023,2024 T-Systems International GmbH
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.configuration.properties.PCFAssetStaticPropertyHolder;
import org.eclipse.tractusx.sde.common.configuration.properties.SDEConfigurationProperties;
import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.utils.PolicyOperationUtil;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.eclipse.tractusx.sde.core.utils.ValueReplacerUtility;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConstant;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.CreateEDCAssetFacilator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("default")
public class PCFExchangeAssetProvider {

	private static final String REGISTRY_TYPE = "registryType";
	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final CreateEDCAssetFacilator createEDCAssetFacilator;
	private final ValueReplacerUtility valueReplacerUtility;
	private final SDEConfigurationProperties sdeConfigurationProperties;
	private final PCFAssetStaticPropertyHolder pcfAssetStaticPropertyHolder;

	@PostConstruct
	@SneakyThrows
	public void init() {

		String assetId = UUIdGenerator.getUuid();
		AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest("", "PCF Exchange endpoint information",
				assetId, "1", "", "", pcfAssetStaticPropertyHolder.getSematicId(), pcfAssetStaticPropertyHolder.getAssetPropTypePCFExchangeType());

		String baseUrl = sdeConfigurationProperties.getSdeHostname() + "/pcf";
		assetEntryRequest.getDataAddress().getProperties().put("baseUrl", baseUrl);
		assetEntryRequest.getProperties().put(REGISTRY_TYPE, baseUrl);
		assetEntryRequest.getProperties().put(EDCAssetConstant.CX_COMMON_VERSION, "1.1");
		

		Map<String, String> inputData = new HashMap<>();
		inputData.put("baseUrl", baseUrl);
		inputData.put(REGISTRY_TYPE, REGISTRY_TYPE);
		inputData.put("assetType", pcfAssetStaticPropertyHolder.getAssetPropTypePCFExchangeType());

		ObjectNode requestBody = (ObjectNode) new ObjectMapper().readTree(valueReplacerUtility
				.valueReplacerUsingFileTemplate("/edc_request_template/edc_asset_lookup.json", inputData));

		JsonNode assetExistsLookupBasedOnTypeGetAsAsset = edcGateway.assetExistsLookupBasedOnTypeGetAsAsset(requestBody);
		
		if (assetExistsLookupBasedOnTypeGetAsAsset == null || 
				assetExistsLookupBasedOnTypeGetAsAsset.isNull() || 
				(assetExistsLookupBasedOnTypeGetAsAsset.isArray() && assetExistsLookupBasedOnTypeGetAsAsset.isEmpty())) {

			List<String> pcfBPNList = pcfAssetStaticPropertyHolder.getWhiteListBusinessList();

			//iterate over pcfBPNList and create business partner group
			for (String pcfBPN : pcfBPNList) {
				edcGateway.addBPNintoPCFBusinessPartnerGroup(pcfBPN, pcfAssetStaticPropertyHolder.getPcfBusinessPartnerGroup());
			}

			List<Policies> accessPolicy = PolicyOperationUtil
					.getStringPolicyAsPolicyList(pcfAssetStaticPropertyHolder.getPcfExchangeAccessPolicy());
			
			List<Policies> usagePolicy = PolicyOperationUtil
					.getStringPolicyAsPolicyList(pcfAssetStaticPropertyHolder.getPcfExchangeUsagePolicy());
			
			PolicyModel policy= PolicyModel.builder()
					.accessPolicies(accessPolicy)
					.usagePolicies(usagePolicy)
					.build();
			
			Map<String, String> createEDCAsset = createEDCAssetFacilator.createEDCAsset(assetEntryRequest, policy);
			
			pcfAssetStaticPropertyHolder.setPcfExchangeAssetId(assetEntryRequest.getId());
			log.info("PCF Exchange asset creates :" + createEDCAsset.toString());
			
		} else {
			
			pcfAssetStaticPropertyHolder.setPcfExchangeAssetId(assetExistsLookupBasedOnTypeGetAsAsset.get(0).get("@id").asText());
			log.info("PCF Exchange asset exists in edc connector, so ignoring asset creation");
		}
	}
}