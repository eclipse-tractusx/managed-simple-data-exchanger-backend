package com.catenax.dft.gateways.external;

import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.ContractDefinitionRequest;
import com.catenax.dft.entities.edc.request.policies.PolicyDefinitionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EDCAssetGateway extends EDCGatewayImpl {

    @Value(value = "${edc.aspect.url}")
    private String edcEndpoint;
    @Value(value = "${edc.aspect.apiKey}")
    private String apiKey;
    @Value(value = "${edc.aspect.apiValue}")
    private String apiValue;

    @Override
    public boolean assetLookup(String id) {
        final String url = edcEndpoint + "/assets/"+ id;
        return checkIfAssetExists(url, apiKey, apiValue);
    }

    @Override
    public void createAsset(AssetEntryRequest request) {
        final String url = edcEndpoint + "/assets/";
        postAsset(url,apiKey,apiValue,request);
    }

    @Override
    public void createPolicyDefinition(PolicyDefinitionRequest request) {
        postPolicyDefinition(edcEndpoint, apiKey, apiValue, request);
    }

    @Override
    public void createContractDefinition(ContractDefinitionRequest request) {
        postContractDefinition(edcEndpoint, apiKey, apiValue, request);
    }
}
