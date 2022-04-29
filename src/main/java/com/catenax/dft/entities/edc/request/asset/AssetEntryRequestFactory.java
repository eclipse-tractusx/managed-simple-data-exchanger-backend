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

import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.entities.usecases.AspectRelationship;
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
    private final String END_POINT = "http://aas-server:port/shells/" +
            "urn%3Auuid%3Ad60b99b0-f269-42f5-94d0-64fe0946ed04/aas/" +
            "submodels/urn%3Auuid%3A53125dc3-5e6f-4f4b-838d-447432b97918/" +
            "submodel&content=value&extent=WithBLOBValue";

    public AssetEntryRequest getAsset(Aspect input) {
        String propId = input.getShellId() + "-" + input.getSubModelId();

        HashMap<String, String> assetProperties = getAssetProperties(propId);
        assetProperties.put("asset:prop:name", ASSET_PROP_NAME_ASPECT_RELATIONSHIP);
        AssetRequest assetRequest = AssetRequest.builder().properties(assetProperties).build();

        HashMap<String, String> dataAddressProperties = getDataAddressProperties();
        DataAddressRequest dataAddressRequest = DataAddressRequest.builder().properties(dataAddressProperties).build();

        return AssetEntryRequest.builder().asset(assetRequest)
                .dataAddress(dataAddressRequest).build();
    }

    public AssetEntryRequest getAsset(AspectRelationship input) {
        String propId = input.getShellId() + "-" + input.getSubModelId();

        HashMap<String, String> assetProperties = getAssetProperties(propId);
        assetProperties.put("asset:prop:name", ASSET_PROP_NAME_ASPECT);
        AssetRequest assetRequest = AssetRequest.builder().properties(assetProperties).build();

        HashMap<String, String> dataAddressProperties = getDataAddressProperties();
        DataAddressRequest dataAddressRequest = DataAddressRequest.builder().properties(dataAddressProperties).build();

        return AssetEntryRequest.builder().asset(assetRequest)
                .dataAddress(dataAddressRequest).build();
    }

    private HashMap<String, String> getAssetProperties(String propId) {
        HashMap<String, String> assetProperties = new HashMap<>();
        assetProperties.put("asset:prop:id", propId);
        assetProperties.put("asset:prop:contenttype", ASSET_PROP_CONTENT_TYPE);
        assetProperties.put("asset:prop:name", ASSET_PROP_NAME_ASPECT);
        assetProperties.put("asset:prop:description", ASSET_PROP_DESCRIPTION);
        assetProperties.put("asset:prop:version", ASSET_PROP_VERSION);
        return assetProperties;
    }

    private HashMap<String, String> getDataAddressProperties(){
        HashMap<String, String> dataAddressProperties = new HashMap<>();
        dataAddressProperties.put("type", TYPE);
        dataAddressProperties.put("endpoint", END_POINT);
        dataAddressProperties.put("name", NAME);
        dataAddressProperties.put("authKey", AUTH_KEY);
        dataAddressProperties.put("authCode", AUTH_CODE);
        return dataAddressProperties;
    }
}
