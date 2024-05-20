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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.tractusx.sde.common.configuration.properties.DigitalTwinConfigurationProperties;
import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.utils.PolicyOperationUtil;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.eclipse.tractusx.sde.core.utils.ValueReplacerUtility;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConfigurableConstant;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.CreateEDCAssetFacilator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.core.JsonProcessingException;
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
public class DigitalTwinAssetProvider {

	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final CreateEDCAssetFacilator createEDCAssetFacilator;
	private final DigitalTwinConfigurationProperties digitalTwinConfigurationProperties;
	private final ValueReplacerUtility valueReplacerUtility;
	private final EDCAssetConfigurableConstant edcAssetConfigurableConstant;

	@PostConstruct
	@SneakyThrows
	public void init() {

		if (digitalTwinConfigurationProperties.getDigitalTwinsRegistryPath()
				.equals(digitalTwinConfigurationProperties.getDigitalTwinsLookupPath())) {
			create("registry", digitalTwinConfigurationProperties.getDigitalTwinsRegistryPath());
		} else {
			create("regisry-api", digitalTwinConfigurationProperties.getDigitalTwinsRegistryPath());
			create("discovery-api", digitalTwinConfigurationProperties.getDigitalTwinsLookupPath());
		}
	}

	private void create(String registryType, String registryAPI) throws JsonProcessingException {

		String assetId = UUIdGenerator.getUuid();

		AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest("", "Digital twin registry information",
				assetId, "1", "", "", "", edcAssetConfigurableConstant.getAssetPropTypeDigitalTwin());

		String baseUrl = digitalTwinConfigurationProperties.getDigitalTwinsHostname() + registryAPI;

		assetEntryRequest.getProperties().put(registryType, baseUrl);

		assetEntryRequest.getDataAddress().getProperties().put("baseUrl", baseUrl);

		assetEntryRequest.getDataAddress().getProperties().put("oauth2:tokenUrl",
				digitalTwinConfigurationProperties.getDigitalTwinsAuthenticationUrl());

		assetEntryRequest.getDataAddress().getProperties().put("oauth2:clientId",
				digitalTwinConfigurationProperties.getDigitalTwinsAuthenticationClientId());

		assetEntryRequest.getDataAddress().getProperties().put("oauth2:clientSecret",
				digitalTwinConfigurationProperties.getDigitalTwinsAuthenticationClientSecret());

		if (assetEntryRequest.getDataAddress().getProperties().containsKey("oauth2:clientSecretKey")) {
			assetEntryRequest.getDataAddress().getProperties().remove("oauth2:clientSecretKey");
		}

		if (StringUtils.isNotBlank(digitalTwinConfigurationProperties.getDigitalTwinsAuthenticationScope())) {
			assetEntryRequest.getDataAddress().getProperties().put("oauth2:scope",
					digitalTwinConfigurationProperties.getDigitalTwinsAuthenticationScope());
		}

		Map<String, String> inputData = new HashMap<>();
		inputData.put("baseUrl", baseUrl);
		inputData.put("registryType", registryType);
		inputData.put("assetType", edcAssetConfigurableConstant.getAssetPropTypeDigitalTwin());

		ObjectNode requestBody = (ObjectNode) new ObjectMapper().readTree(valueReplacerUtility
				.valueReplacerUsingFileTemplate("/edc_request_template/edc_asset_lookup.json", inputData));

		if (!edcGateway.assetExistsLookupBasedOnType(requestBody)) {

			List<Policies> accessPolicy = PolicyOperationUtil
					.getStringPolicyAsPolicyList(edcAssetConfigurableConstant.getDigitalTwinExchangeAccessPolicy());
			
			List<Policies> usagePolicy = PolicyOperationUtil
					.getStringPolicyAsPolicyList(edcAssetConfigurableConstant.getDigitalTwinExchangeUsagePolicy());
			
			PolicyModel policy = PolicyModel.builder().accessPolicies(accessPolicy)
					.usagePolicies(usagePolicy)
					.build();

			Map<String, String> createEDCAsset = createEDCAssetFacilator.createEDCAsset(assetEntryRequest, policy);
			log.info("Digital twin " + registryType + " asset creates :" + createEDCAsset.toString());
		} else {
			log.info("Digital twin " + registryType + " asset exists in edc connector, so ignoring asset creation");
		}
	}

	@SneakyThrows
	private String valueReplacer(String requestTemplatePath, Map<String, String> inputData) {
		StringSubstitutor stringSubstitutor1 = new StringSubstitutor(inputData);
		return stringSubstitutor1.replace(requestTemplatePath);
	}
}