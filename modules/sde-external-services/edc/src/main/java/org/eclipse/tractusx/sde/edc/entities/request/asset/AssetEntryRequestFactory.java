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

package org.eclipse.tractusx.sde.edc.entities.request.asset;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConfigurableConstant;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class AssetEntryRequestFactory {

	@Value(value = "${dft.hostname}")
	private String dftHostname;

	@Value(value = "${manufacturerId}")
	private String manufacturerId;

	@Value(value = "${edc.hostname}")
	private String edcEndpoint;

	@Value(value = "${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/token")
	private String idpIssuerTokenURL;

	@Value(value = "${digital-twins.authentication.clientId}")
	private String clientId;

	private final EDCAssetConfigurableConstant edcAssetConfigurableConstant;

	@SneakyThrows
	public String createAssetId(String shellId, String subModelId) {
		return shellId + "-" + subModelId;
	}

	public AssetEntryRequest getAssetRequest(String submodel, String assetName, String shellId, String subModelId,
			String submoduleUriPath, String uuid, String sematicId, String dctType) {

		String assetId = createAssetId(shellId, subModelId);

		HashMap<String, Object> assetProperties = getAssetProperties(assetId, assetName, sematicId, dctType);

		String uriString = subModelPayloadUrl(submodel, submoduleUriPath, uuid);

		HashMap<String, String> dataAddressProperties = getDataAddressProperties(shellId, subModelId, uriString);
		DataAddressRequest dataAddressRequest = DataAddressRequest.builder().properties(dataAddressProperties).build();

		return AssetEntryRequest.builder().id(assetId).properties(assetProperties).dataAddress(dataAddressRequest)
				.build();
	}

	private String subModelPayloadUrl(String submodel, String submoduleUriPath, String uuid) {
		return UriComponentsBuilder.fromHttpUrl(dftHostname).path("/" + submodel + "/" + submoduleUriPath)
				.path("/" + uuid).toUriString();
	}

	private HashMap<String, Object> getAssetProperties(String assetId, String assetName, String sematicId,
			String edcAssetType) {
		HashMap<String, Object> assetProperties = new HashMap<>();
		assetProperties.put(EDCAssetConstant.ASSET_PROP_ID, assetId);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_CONTENTTYPE, EDCAssetConstant.ASSET_PROP_CONTENT_TYPE);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_VERSION, EDCAssetConstant.ASSET_PROP_VERSION_VALUE);
		assetProperties.put(EDCAssetConstant.ASSET_PROP_NAME, assetName);
		assetProperties.put(EDCAssetConstant.RDFS_LABEL, assetName);
		assetProperties.put(EDCAssetConstant.RDFS_COMMENT, assetName);
		assetProperties.put(EDCAssetConstant.DCAT_VERSION, edcAssetConfigurableConstant.getAssetPropDcatVersion());
		assetProperties.put(EDCAssetConstant.CX_COMMON_VERSION,
				edcAssetConfigurableConstant.getAssetPropCommonVersion());

		if (StringUtils.isNotBlank(sematicId))
			assetProperties.put(EDCAssetConstant.AAS_SEMANTICS_SEMANTIC_ID, Map.of("@id", sematicId));

		if (StringUtils.isNotBlank(edcAssetType)) {
			assetProperties.put(EDCAssetConstant.DCT_TYPE,
					Map.of("@id", EDCAssetConstant.CX_TAXO_PREFIX + edcAssetType));
			assetProperties.put(EDCAssetConstant.ASSET_PROP_TYPE, edcAssetType);
		} else {
			assetProperties.put(EDCAssetConstant.ASSET_PROP_TYPE,
					edcAssetConfigurableConstant.getAssetPropTypeDefaultValue());
		}

		return assetProperties;
	}

	private HashMap<String, String> getDataAddressProperties(String shellId, String subModelId, String endpoint) {
		HashMap<String, String> dataAddressProperties = new HashMap<>();
		dataAddressProperties.put("type", EDCAssetConstant.TYPE);
		dataAddressProperties.put("baseUrl", String.format(endpoint, shellId, subModelId));
		dataAddressProperties.put("oauth2:tokenUrl", idpIssuerTokenURL);
		dataAddressProperties.put("oauth2:clientId", clientId);
		dataAddressProperties.put("oauth2:clientSecretKey", "client-secret");
		dataAddressProperties.put("proxyMethod", "true");
		dataAddressProperties.put("proxyBody", "true");
		dataAddressProperties.put("proxyPath", "true");
		dataAddressProperties.put("proxyQueryParams", "true");
		dataAddressProperties.put("contentType", EDCAssetConstant.ASSET_PROP_CONTENT_TYPE);
		return dataAddressProperties;
	}

}