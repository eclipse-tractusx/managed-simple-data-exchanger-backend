/*
 * Copyright 2022 CatenaX
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.catenax.dft.entities.edc.request.asset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AssetEntryRequestFactory {

    private final String ASSET_PROP_CONTENT_TYPE = "application/json";
    private final String ASSET_PROP_NAME_ASPECT = "Serialized Part - Submodel SerialPartTypization";
    private final String ASSET_PROP_NAME_ASPECT_RELATIONSHIP = "Serialized Part - Submodel AssemblyPartRelationship";
    private final String ASSET_PROP_DESCRIPTION = "...";
    private final String ASSET_PROP_VERSION = "1.0.0";
    private final String NAME = "Backend Data Service - AAS Server";
    private final String AUTH_KEY = "";
    private final String AUTH_CODE = "";
    private final String TYPE = "AzureStorage";
    //TODO:encode Url
    @Value(value = "${edc.asset.payload.url}")
    private String END_POINT;

    public AssetEntryRequest getAspectRelationshipAssetRequest(String shellId, String subModelId) {
        return buildAsset(shellId, subModelId, ASSET_PROP_NAME_ASPECT_RELATIONSHIP);
    }

    public AssetEntryRequest getAspectAssetRequest(String shellId, String subModelId) {
        return buildAsset(shellId, subModelId, ASSET_PROP_NAME_ASPECT);
    }

    private AssetEntryRequest buildAsset(String shellId, String subModelId, String assetName) {
        String assetId = shellId + "-" + subModelId;

        HashMap<String, String> assetProperties = getAssetProperties(assetId, assetName);
        AssetRequest assetRequest = AssetRequest.builder().properties(assetProperties).build();

        HashMap<String, String> dataAddressProperties = getDataAddressProperties(shellId, subModelId);
        DataAddressRequest dataAddressRequest = DataAddressRequest.builder().properties(dataAddressProperties).build();

        return AssetEntryRequest.builder()
                .asset(assetRequest)
                .dataAddress(dataAddressRequest)
                .build();
    }

    private HashMap<String, String> getAssetProperties(String assetId, String assetName) {
        HashMap<String, String> assetProperties = new HashMap<>();
        assetProperties.put("asset:prop:id", assetId);
        assetProperties.put("asset:prop:name", assetName);
        assetProperties.put("asset:prop:contenttype", ASSET_PROP_CONTENT_TYPE);
        assetProperties.put("asset:prop:description", ASSET_PROP_DESCRIPTION);
        assetProperties.put("asset:prop:version", ASSET_PROP_VERSION);
        return assetProperties;
    }

    private HashMap<String, String> getDataAddressProperties(String shellId, String subModelId){
        HashMap<String, String> dataAddressProperties = new HashMap<>();
        dataAddressProperties.put("type", TYPE);
        dataAddressProperties.put("endpoint", String.format(END_POINT, shellId, subModelId));
        dataAddressProperties.put("name", NAME);
        dataAddressProperties.put("authKey", AUTH_KEY);
        dataAddressProperties.put("authCode", AUTH_CODE);
        return dataAddressProperties;
    }
}
