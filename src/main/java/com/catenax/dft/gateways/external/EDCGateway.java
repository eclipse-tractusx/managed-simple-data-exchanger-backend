package com.catenax.dft.gateways.external;

import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.ContractDefinitionRequest;
import com.catenax.dft.entities.edc.request.policies.PolicyDefinitionRequest;

public interface EDCGateway {

    boolean assetLookup(String id);

    void createAsset(AssetEntryRequest request);

    void createPolicyDefinition(PolicyDefinitionRequest request);

    void createContractDefinition(ContractDefinitionRequest request);
}
