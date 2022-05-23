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

package com.catenax.dft.usecases.csvHandler.aspectRelationship;

import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.asset.AssetEntryRequestFactory;
import com.catenax.dft.entities.edc.request.contractDefinition.ContractDefinitionRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.ContractDefinitionRequestFactory;
import com.catenax.dft.entities.edc.request.policies.PolicyDefinitionRequest;
import com.catenax.dft.entities.edc.request.policies.PolicyRequestFactory;
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.gateways.external.EDCAssetChildGateway;
import com.catenax.dft.gateways.external.EDCGateway;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EDCAspectRelationshipHandlerUseCase extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private AssetEntryRequestFactory assetFactory;
    private EDCAssetChildGateway edcGateway;
    private PolicyRequestFactory policyFactory;
    private ContractDefinitionRequestFactory contractFactory;

    public EDCAspectRelationshipHandlerUseCase(StoreAspectRelationshipCsvHandlerUseCase nextUseCase,
                                               EDCAssetChildGateway edcGateway,
                                               AssetEntryRequestFactory assetFactory,
                                               PolicyRequestFactory policyFactory,
                                               ContractDefinitionRequestFactory contractFactory) {
        super(nextUseCase);
        this.assetFactory = assetFactory;
        this.edcGateway = edcGateway;
        this.policyFactory = policyFactory;
        this.contractFactory = contractFactory;
    }

    @SneakyThrows
    @Override
    protected AspectRelationship executeUseCase(AspectRelationship input, String processId) {
        String shellId = input.getShellId();
        String subModelId = input.getSubModelId();

        //Create asset
        AssetEntryRequest assetEntryRequest = assetFactory.getAspectRelationshipAssetRequest(shellId, subModelId, input.getParentUuid());
        if (!edcGateway.assetLookup(assetEntryRequest.getAsset().getProperties().get("asset:prop:id"))) {
            edcGateway.createAsset(assetEntryRequest);

            //create policies
            PolicyDefinitionRequest policyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId);
            edcGateway.createPolicyDefinition(policyDefinitionRequest);

            //create contractDefinitions
            ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(
                    assetEntryRequest.getAsset().getProperties().get("asset:prop:id"),
                    policyDefinitionRequest.getUid());
            edcGateway.createContractDefinition(contractDefinitionRequest);
        }

        return input;
    }
}
