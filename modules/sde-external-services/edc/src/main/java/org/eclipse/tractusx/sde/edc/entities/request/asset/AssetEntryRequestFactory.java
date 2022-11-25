/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.eclipse.tractusx.sde.edc.constants.EDCAssetConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AssetEntryRequestFactory {

    private static final String ASSET_PROP_CONTENT_TYPE = "application/json";
    private static final String ASSET_PROP_VERSION = "1.0.0";
    private static final String NAME = "Backend Data Service - AAS Server";
    private static final String TYPE = "HttpData";
    private static final String DATE_FORMATTER = "dd/MM/yyyy HH:mm:ss";
    @Value(value = "${dft.apiKeyHeader}")
    private String apiKeyHeader;
    @Value(value = "${dft.apiKey}")
    private String apiKey;
    @Value(value = "${dft.hostname}")
    private String dftHostname;
    @Value(value = "${manufacturerId}")
    private String manufacturerId;
    @Value(value = "${edc.hostname}")
    private String edcEndpoint;

    
    public AssetEntryRequest getAssetRequest(String submodel, String assetName, String shellId, String subModelId, String uuid) {
        return buildAsset(submodel, shellId, subModelId, assetName, uuid);
    }

    private AssetEntryRequest buildAsset(String submodel, String shellId, String subModelId, String assetName, String uuid) {
        String assetId = shellId + "-" + subModelId;

        HashMap<String, String> assetProperties = getAssetProperties(assetId, assetName);
        AssetRequest assetRequest = AssetRequest.builder().properties(assetProperties).build();

        String uriString = subModelPayloadUrl(submodel, uuid);

        HashMap<String, String> dataAddressProperties = getDataAddressProperties(shellId, subModelId, uriString);
        DataAddressRequest dataAddressRequest = DataAddressRequest.builder().properties(dataAddressProperties).build();

        return AssetEntryRequest.builder()
                .asset(assetRequest)
                .dataAddress(dataAddressRequest)
                .build();
    }

    private String subModelPayloadUrl(String submodel, String uuid) {
    	 return UriComponentsBuilder
                 .fromHttpUrl(dftHostname)
                 .path("/api/"+submodel+"/public/")
                 .path(uuid)
                 .toUriString();
	}

	private HashMap<String, String> getAssetProperties(String assetId, String assetName) {
        HashMap<String, String> assetProperties = new HashMap<>();
        LocalDateTime d = LocalDateTime.now();
        String date = d.format(DateTimeFormatter.ofPattern(DATE_FORMATTER));
        assetProperties.put(EDCAssetConstant.ASSET_PROP_ID, assetId);
        assetProperties.put(EDCAssetConstant.ASSET_PROP_NAME, assetName);
        assetProperties.put(EDCAssetConstant.ASSET_PROP_CONTENTTYPE, ASSET_PROP_CONTENT_TYPE);
        assetProperties.put(EDCAssetConstant.ASSET_PROP_DESCRIPTION, assetName);
        assetProperties.put(EDCAssetConstant.ASSET_PROP_VERSION, ASSET_PROP_VERSION);
        assetProperties.put(EDCAssetConstant.ASSET_PROP_PUBLISHER, manufacturerId+":"+edcEndpoint);
        assetProperties.put(EDCAssetConstant.ASSET_PROP_CREATED, date);
        assetProperties.put(EDCAssetConstant.ASSET_PROP_MODIFIED, date);
        return assetProperties;
    }

    private HashMap<String, String> getDataAddressProperties(String shellId, String subModelId, String endpoint) {
        HashMap<String, String> dataAddressProperties = new HashMap<>();
        dataAddressProperties.put("type", TYPE);
        dataAddressProperties.put("baseUrl", String.format(endpoint, shellId, subModelId));
        dataAddressProperties.put("name", NAME);
        dataAddressProperties.put("authKey", apiKeyHeader);
        dataAddressProperties.put("authCode", apiKey);
        return dataAddressProperties;
    }

 }
