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
import com.catenax.dft.entities.edc.request.contractDefinition.CreateContractDefinitionRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.Criterion;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.external.EDCGateway;
import com.catenax.dft.mapper.AssetEntryRequestMapper;
import com.catenax.dft.usecases.common.UUIdGenerator;
import com.catenax.dft.usecases.csvHandler.AbstractCsvHandlerUseCase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EDCAspectHandlerUseCase extends AbstractCsvHandlerUseCase<Aspect, Aspect> {

    @Autowired
    private AssetEntryRequestMapper assetMapper;
    @Autowired
    private EDCGateway edcGateway;


    public EDCAspectHandlerUseCase(StoreAspectCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    @SneakyThrows
    @Override
    protected Aspect executeUseCase(Aspect input, String processId) {

        AssetEntryRequest assetEntryRequest = assetMapper.getAsset(input.getShellId() + "-" + input.getSubModelId());
        edcGateway.createAsset(assetEntryRequest);
        //create policies

        //create contractDefinitions
        List<Criterion> criterias = new ArrayList<>();
        criterias.add(Criterion.builder()
                .left("asset.prop.id")
                .op("in")
                .right(input.getUuid())
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
