/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.catenax.dft.usecases.csvHandler.aspects;


import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.asset.AssetEntryRequestFactory;
import com.catenax.dft.entities.edc.request.contractDefinition.ContractDefinitionRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.ContractDefinitionRequestFactory;
import com.catenax.dft.entities.edc.request.policies.PolicyDefinitionRequest;
import com.catenax.dft.entities.edc.request.policies.PolicyRequestFactory;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.external.EDCGateway;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EDCAspectHandlerUseCase extends AbstractCsvHandlerUseCase<Aspect, Aspect> {

    private AssetEntryRequestFactory assetFactory;
    private EDCGateway edcGateway;
    private PolicyRequestFactory policyFactory;
    private ContractDefinitionRequestFactory contractFactory;


    public EDCAspectHandlerUseCase(StoreAspectCsvHandlerUseCase nextUseCase,
                                   EDCGateway edcGateway,
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
    protected Aspect executeUseCase(Aspect input, String processId) {
        String shellId = input.getShellId();
        String subModelId = input.getSubModelId();
        //create asset
        AssetEntryRequest assetEntryRequest = assetFactory.getAspectAssetRequest(shellId, subModelId);
        edcGateway.createAsset(assetEntryRequest, false);

        //create policies
        PolicyDefinitionRequest policyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId);
        edcGateway.createPolicyDefinition(policyDefinitionRequest, false);

        //create contractDefinitions
        ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(input.getUuid());
        edcGateway.createContractDefinition(contractDefinitionRequest, false);

        return input;
    }
}
