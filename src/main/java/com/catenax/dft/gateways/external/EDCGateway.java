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

package com.catenax.dft.gateways.external;


import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.ContractDefinitionRequest;
import com.catenax.dft.entities.edc.request.policies.PolicyDefinitionRequest;
import com.catenax.dft.gateways.exceptions.EDCGatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
public class EDCGateway {

    @Value(value = "${edc.aspect.url}")
    private String edcEndpoint;
    private String API_KEY = "X-Api-Key";
    private String API_VALUE = "123456";

    public void createAsset(AssetEntryRequest request) {
        final String assetResource = "/assets";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(API_KEY, API_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AssetEntryRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(edcEndpoint + assetResource, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new EDCGatewayException(e.getStatusCode().toString());
        }
    }

    public void createPolicyDefinition(PolicyDefinitionRequest request) {
        final String policyResource = "/policies";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(API_KEY, API_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PolicyDefinitionRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(edcEndpoint + policyResource, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new EDCGatewayException(e.getStatusCode().toString());
        }
    }

    public void createContractDefinition(ContractDefinitionRequest request) {
        final String contractDefinitionResource = "/contractdefinitions";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(API_KEY, API_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ContractDefinitionRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(edcEndpoint + contractDefinitionResource, entity, String.class);
        } catch (HttpClientErrorException e) {
            throw new EDCGatewayException(e.getStatusCode().toString());
        }

    }
}
