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
import com.catenax.dft.entities.edc.request.contractDefinition.CreateContractDefinitionRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.Criterion;
import com.catenax.dft.entities.edc.request.policies.PolicyDefinitionRequest;
import com.catenax.dft.entities.edc.request.policies.PolicyRequestFactory;
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.gateways.external.EDCGateway;
import com.catenax.dft.usecases.common.UUIdGenerator;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EDCAspectRelationshipHandlerUseCase extends AbstractCsvHandlerUseCase<AspectRelationship, AspectRelationship> {

    private AssetEntryRequestFactory assetFactory;
    private EDCGateway edcGateway;
    private PolicyRequestFactory policyFactory;

    public EDCAspectRelationshipHandlerUseCase(StoreAspectRelationshipCsvHandlerUseCase nextUseCase, AssetEntryRequestFactory assetFactory, EDCGateway edcGateway, PolicyRequestFactory policyFactory) {
        super(nextUseCase);
        this.assetFactory = assetFactory;
        this.edcGateway = edcGateway;
        this.policyFactory = policyFactory;
    }

    @SneakyThrows
    @Override
    protected AspectRelationship executeUseCase(AspectRelationship input, String processId) {

        //Create asset
        AssetEntryRequest assetEntryRequest = assetFactory.getAsset(input);
        edcGateway.createAsset(assetEntryRequest);

        //create policies
        PolicyDefinitionRequest policyDefinitionRequest = policyFactory.getPolicy(input);
        edcGateway.createPolicyDefinition(policyDefinitionRequest);

        //create contractDefinitions
        List<Criterion> criterias = new ArrayList<>();
        criterias.add(Criterion.builder()
                .left("asset.prop.id")
                .op("in")
                .right(input.getChildUuid())
                .build());
        CreateContractDefinitionRequest createContractDefinitionRequest = CreateContractDefinitionRequest.builder()
                .contractPolicyId("")
                .accessPolicyId("")
                .id(UUIdGenerator.getUuid())
                .criteria(criterias)
                .build();
        edcGateway.createContractDefinition(createContractDefinitionRequest);

        return input;
    }
}
