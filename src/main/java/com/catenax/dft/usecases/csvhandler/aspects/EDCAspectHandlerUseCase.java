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

package com.catenax.dft.usecases.csvhandler.aspects;


import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.asset.AssetEntryRequestFactory;
import com.catenax.dft.entities.edc.request.contractdefinition.ContractDefinitionRequest;
import com.catenax.dft.entities.edc.request.contractdefinition.ContractDefinitionRequestFactory;
import com.catenax.dft.entities.edc.request.policies.PolicyDefinitionRequest;
import com.catenax.dft.entities.edc.request.policies.PolicyRequestFactory;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.external.EDCAssetGateway;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EDCAspectHandlerUseCase extends AbstractCsvHandlerUseCase<Aspect, Aspect> {

    private final AssetEntryRequestFactory assetFactory;
    private final EDCAssetGateway edcGateway;
    private final PolicyRequestFactory policyFactory;
    private final ContractDefinitionRequestFactory contractFactory;


    public EDCAspectHandlerUseCase(StoreAspectCsvHandlerUseCase nextUseCase,
                                   EDCAssetGateway edcGateway,
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

        try {


            //create asset
            AssetEntryRequest assetEntryRequest = assetFactory.getAspectAssetRequest(shellId, subModelId, input.getUuid());
            if (!edcGateway.assetExistsLookup(assetEntryRequest.getAsset().getProperties().get("asset:prop:id"))) {
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
        } catch (Exception e) {
            throw new CsvHandlerUseCaseException(input.getRowNumber(), "EDC: " + e.getMessage());
        }
    }
}
