/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.CreateEDCAssetFacilator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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

	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final CreateEDCAssetFacilator createEDCAssetFacilator;

	@Value("${dft.hostname}")
	private String sdeHostname;

	private static String assetFilterRequest = """
			{
				    "@context": {
				        "edc": "https://w3id.org/edc/v0.0.1/ns/"
				    },
				    "@type": "QuerySpec",
				    "offset": 0,
				    "limit": 10,
				    "sortOrder": "DESC",
				    "sortField": "id",
				    "filterExpression": [
				        {
				            "edc:operandLeft": "https://w3id.org/edc/v0.0.1/ns/type",
				            "edc:operator": "=",
				            "edc:operandRight": "data.pcf.exchangeEndpoint"
				        }
				    ]
				}
			""";

	@PostConstruct
	@SneakyThrows
	public void init() {

		String assetId = UUIdGenerator.getUuid();
		AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest("", "PCF Exchange endpoint information",
				assetId, "1", "");

		assetEntryRequest.getAsset().getProperties().put("type", "data.pcf.exchangeEndpoint");
		assetEntryRequest.getDataAddress().getProperties().put("baseUrl", sdeHostname+"/pcf");
		ObjectNode requestBody = (ObjectNode) new ObjectMapper().readTree(assetFilterRequest);

		if (!edcGateway.assetExistsLookupBasedOnType(requestBody)) {
			Map<String, String> createEDCAsset = createEDCAssetFacilator.createEDCAsset(assetEntryRequest, List.of(),
					Map.of());
			log.info("PCF Exchange asset creates :" + createEDCAsset.toString());
		} else {
			log.info("PCF Exchange asset exists in edc connector, so ignoring asset creation");
		}
	}

}
