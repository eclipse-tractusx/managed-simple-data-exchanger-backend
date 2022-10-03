/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package com.catenax.dft.service;

import com.catenax.dft.api.ContractOfferCatalogApi;
import com.catenax.dft.entities.UsagePolicy;
import com.catenax.dft.entities.edc.request.policies.ConstraintRequest;
import com.catenax.dft.entities.edc.request.policies.Expression;
import com.catenax.dft.entities.edc.request.policies.PolicyConstraintBuilderService;
import com.catenax.dft.enums.NegotiationState;
import com.catenax.dft.enums.PolicyAccessEnum;
import com.catenax.dft.enums.Type;
import com.catenax.dft.enums.UsagePolicyEnum;
import com.catenax.dft.facilitator.ContractNegotiateManagement;
import com.catenax.dft.gateways.database.ContractNegotiationInfoRepository;
import com.catenax.dft.model.contractnegotiation.ContractAgreementResponse;
import com.catenax.dft.model.contractnegotiation.ContractNegotiationDto;
import com.catenax.dft.model.contractoffers.ContractOffersCatalogResponse;
import com.catenax.dft.model.request.ConsumerRequest;
import com.catenax.dft.model.request.OfferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ConsumerControlPanelService.class, String.class})
@ExtendWith(SpringExtension.class)
class ConsumerControlPanelServiceTest {
    @MockBean
    private ContractNegotiateManagement contractNegotiateManagement;

    @MockBean
    private ContractNegotiationInfoRepository contractNegotiationInfoRepository;

    @Autowired
    private ConsumerControlPanelService consumerControlPanelService;

    @MockBean
    private ContractOfferCatalogApi contractOfferCatalogApi;

    @MockBean
    private PolicyConstraintBuilderService policyConstraintBuilderService;


    @Test
    void testQueryOnDataOfferEmpty() throws Exception {
        ContractOffersCatalogResponse contractOffersCatalogResponse = new ContractOffersCatalogResponse();
        contractOffersCatalogResponse.setContractOffers(new ArrayList<>());
        when(contractOfferCatalogApi.getContractOffersCatalog((Map<String, String>) any(), (String) any(), anyInt()))
                .thenReturn(contractOffersCatalogResponse);
        assertTrue(consumerControlPanelService.queryOnDataOffers("https://example.org/example").isEmpty());
        verify(contractOfferCatalogApi).getContractOffersCatalog((Map<String, String>) any(), (String) any(), anyInt());
    }

    @Test
    void testQueryOnDataOffersWithUsagePolicies() throws Exception {

        ContractOffersCatalogResponse contractOffersCatalogResponse = getContractOffersCatalogWithConstraints();
        when(contractOfferCatalogApi.getContractOffersCatalog((Map<String, String>) any(), (String) any(), anyInt()))
                .thenReturn(contractOffersCatalogResponse);
        assertEquals(1, consumerControlPanelService.queryOnDataOffers("https://example.org/example").size());
        verify(contractOfferCatalogApi).getContractOffersCatalog((Map<String, String>) any(), (String) any(), anyInt());
    }

    @Test
    void testQueryOnDataOffersWithMissingUsagePolicies() throws Exception {

        ContractOffersCatalogResponse contractOffersCatalogResponse = getCatalogObjectWithMissingConstraints();
        when(contractOfferCatalogApi.getContractOffersCatalog((Map<String, String>) any(), (String) any(), anyInt()))
                .thenReturn(contractOffersCatalogResponse);
        assertEquals(1, consumerControlPanelService.queryOnDataOffers("https://example.org/example").size());
        verify(contractOfferCatalogApi).getContractOffersCatalog((Map<String, String>) any(), (String) any(), anyInt());
    }

    @Test
    void testSubscribeDataOffers1() {
        ArrayList<OfferRequest> offerRequestList = new ArrayList<>();
        List<UsagePolicy> usagePolicies = new ArrayList<>();
        UsagePolicy usagePolicy = UsagePolicy.builder().type(UsagePolicyEnum.CUSTOM).value("Sample").typeOfAccess(PolicyAccessEnum.RESTRICTED).build();
        usagePolicies.add(usagePolicy);
        ConsumerRequest consumerRequest = new ConsumerRequest("42", "https://example.org/example", offerRequestList,
                usagePolicies);
        String processId = UUID.randomUUID().toString();
        consumerControlPanelService.subscribeDataOffers(consumerRequest, processId);
        assertEquals("42", consumerRequest.getConnectorId());
        assertEquals("https://example.org/example", consumerRequest.getProviderUrl());
        List<UsagePolicy> policies = consumerRequest.getPolicies();
        List<ConstraintRequest> list = new ArrayList<>();
        ConstraintRequest constraintRequest = ConstraintRequest.builder().edcType("type").leftExpression(new Expression()).rightExpression(new Expression()).operator("EQ").build();
        list.add(constraintRequest);
        when(policyConstraintBuilderService.getUsagePolicyConstraints(any())).thenReturn(list);
        assertEquals(usagePolicies, policies);
        assertEquals(1, consumerControlPanelService.getAuthHeader().size());
    }

    @Test
    void testContractOffer() {
        List<ContractNegotiationDto> contractNegotiationDtoList = List.of(ContractNegotiationDto.builder().contractAgreementId(null)
                .id("negotiationId").state(NegotiationState.DECLINED.name()).counterPartyAddress("address").build());
        when(contractNegotiateManagement.getAllContractNegotiations(anyInt(), anyInt())).thenReturn(contractNegotiationDtoList);
        List<ContractAgreementResponse> list = consumerControlPanelService.getAllContractOffers(10,1);
        assertEquals(1, list.size());
    }

    @Test
    void testSubscribeDataOffers2() {
        List<UsagePolicy> usagePolicies = new ArrayList<>();
        UsagePolicy usagePolicy = UsagePolicy.builder().type(UsagePolicyEnum.CUSTOM).value("Sample").typeOfAccess(PolicyAccessEnum.RESTRICTED).build();
        usagePolicies.add(usagePolicy);
        ConsumerRequest consumerRequest = mock(ConsumerRequest.class);
        when(consumerRequest.getOffers()).thenReturn(new ArrayList<>());
        when(consumerRequest.getProviderUrl()).thenReturn("https://example.org/example");
        when(consumerRequest.getPolicies()).thenReturn(usagePolicies);
        String processId = UUID.randomUUID().toString();
        consumerControlPanelService.subscribeDataOffers(consumerRequest, processId);
        verify(consumerRequest).getProviderUrl();
        verify(consumerRequest).getOffers();
    }

    private ContractOffersCatalogResponse getContractOffersCatalogWithConstraints() throws Exception {
        String contactOfferCatalogResponse = "{\n" +
                "    \"id\": \"default\",\n" +
                "    \"contractOffers\": [ " +
                "       {\n" +
                "            \"id\": \"5ff29ec1-90ae-4e75-adf1-afb19ed8c686:cf0fc8c3-c7d6-4b52-baac-764f62312778\",\n" +
                "            \"policy\": {\n" +
                "                \"uid\": \"5dc869fc-9b30-4622-8e25-310033b3b927\",\n" +
                "                \"permissions\": [\n" +
                "                    {\n" +
                "                        \"edctype\": \"dataspaceconnector:permission\",\n" +
                "                        \"uid\": null,\n" +
                "                        \"target\": \"urn:uuid:4cfaf444-ba35-4971-9b02-162a1c8cdfe5-urn:uuid:7b5a2bfc-36ae-49b3-a7a1-c3bb6cfed190\",\n" +
                "                        \"action\": {\n" +
                "                            \"type\": \"USE\",\n" +
                "                            \"includedIn\": null,\n" +
                "                            \"constraint\": null\n" +
                "                        },\n" +
                "                        \"assignee\": null,\n" +
                "                        \"assigner\": null,\n" +
                "                        \"constraints\": [\n" +
                "                            {\n" +
                "                                \"edctype\": \"AtomicConstraint\",\n" +
                "                                \"leftExpression\": {\n" +
                "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n" +
                "                                    \"value\": \"BusinessPartnerNumber\"\n" +
                "                                },\n" +
                "                                \"rightExpression\": {\n" +
                "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n" +
                "                                    \"value\": \"[BPNL00000005CONS, BPNL00000005PROV]\"\n" +
                "                                },\n" +
                "                                \"operator\": \"IN\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"edctype\": \"AtomicConstraint\",\n" +
                "                                \"leftExpression\": {\n" +
                "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n" +
                "                                    \"value\": \"idsc:ELAPSED_TIME\"\n" +
                "                                },\n" +
                "                                \"rightExpression\": {\n" +
                "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n" +
                "                                    \"value\": \"P0Y0M3DT00H00M00S\"\n" +
                "                                },\n" +
                "                                \"operator\": \"LEQ\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"edctype\": \"AtomicConstraint\",\n" +
                "                                \"leftExpression\": {\n" +
                "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n" +
                "                                    \"value\": \"idsc:ROLE\"\n" +
                "                                },\n" +
                "                                \"rightExpression\": {\n" +
                "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n" +
                "                                    \"value\": \"ADMIN\"\n" +
                "                                },\n" +
                "                                \"operator\": \"EQ\"\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"duties\": []\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"prohibitions\": [],\n" +
                "                \"obligations\": [],\n" +
                "                \"extensibleProperties\": {},\n" +
                "                \"inheritsFrom\": null,\n" +
                "                \"assigner\": null,\n" +
                "                \"assignee\": null,\n" +
                "                \"target\": null,\n" +
                "                \"@type\": {\n" +
                "                    \"@policytype\": \"set\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"asset\": {\n" +
                "                \"properties\": {\n" +
                "                    \"asset:prop:name\": \"Serialized Part - Submodel SerialPartTypization\",\n" +
                "                    \"asset:prop:contenttype\": \"application/json\",\n" +
                "                    \"asset:prop:description\": \"Serialized Part - Submodel SerialPartTypization\",\n" +
                "                    \"ids:byteSize\": null,\n" +
                "                    \"asset:prop:version\": \"1.0.0\",\n" +
                "                    \"asset:prop:id\": \"urn:uuid:4cfaf444-ba35-4971-9b02-162a1c8cdfe5-urn:uuid:7b5a2bfc-36ae-49b3-a7a1-c3bb6cfed190\",\n" +
                "                    \"ids:fileName\": null\n" +
                "                }\n" +
                "            },\n" +
                "            \"policyId\": null,\n" +
                "            \"assetId\": null,\n" +
                "            \"provider\": \"urn:connector:provider\",\n" +
                "            \"consumer\": \"urn:connector:consumer\",\n" +
                "            \"offerStart\": null,\n" +
                "            \"offerEnd\": null,\n" +
                "            \"contractStart\": null,\n" +
                "            \"contractEnd\": null\n" +
                "                }\n" +
                "       ]\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        ContractOffersCatalogResponse mockResponse
                = mapper.readValue(contactOfferCatalogResponse, ContractOffersCatalogResponse.class);
        return mockResponse;
    }

    private ContractOffersCatalogResponse getCatalogObjectWithMissingConstraints() throws Exception {
        String contactOfferCatalogResponse = "{\n" +
                "    \"id\": \"default\",\n" +
                "    \"contractOffers\": [ " +
                "       {\n" +
                "            \"id\": \"5ff29ec1-90ae-4e75-adf1-afb19ed8c686:cf0fc8c3-c7d6-4b52-baac-764f62312778\",\n" +
                "            \"policy\": {\n" +
                "                \"uid\": \"5dc869fc-9b30-4622-8e25-310033b3b927\",\n" +
                "                \"permissions\": [\n" +
                "                    {\n" +
                "                        \"edctype\": \"dataspaceconnector:permission\",\n" +
                "                        \"uid\": null,\n" +
                "                        \"target\": \"urn:uuid:4cfaf444-ba35-4971-9b02-162a1c8cdfe5-urn:uuid:7b5a2bfc-36ae-49b3-a7a1-c3bb6cfed190\",\n" +
                "                        \"action\": {\n" +
                "                            \"type\": \"USE\",\n" +
                "                            \"includedIn\": null,\n" +
                "                            \"constraint\": null\n" +
                "                        },\n" +
                "                        \"assignee\": null,\n" +
                "                        \"assigner\": null,\n" +
                "                        \"constraints\": [],\n" +
                "                        \"duties\": []\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"prohibitions\": [],\n" +
                "                \"obligations\": [],\n" +
                "                \"extensibleProperties\": {},\n" +
                "                \"inheritsFrom\": null,\n" +
                "                \"assigner\": null,\n" +
                "                \"assignee\": null,\n" +
                "                \"target\": null,\n" +
                "                \"@type\": {\n" +
                "                    \"@policytype\": \"set\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"asset\": {\n" +
                "                \"properties\": {\n" +
                "                    \"asset:prop:name\": \"Serialized Part - Submodel SerialPartTypization\",\n" +
                "                    \"asset:prop:contenttype\": \"application/json\",\n" +
                "                    \"asset:prop:description\": \"Serialized Part - Submodel SerialPartTypization\",\n" +
                "                    \"ids:byteSize\": null,\n" +
                "                    \"asset:prop:version\": \"1.0.0\",\n" +
                "                    \"asset:prop:id\": \"urn:uuid:4cfaf444-ba35-4971-9b02-162a1c8cdfe5-urn:uuid:7b5a2bfc-36ae-49b3-a7a1-c3bb6cfed190\",\n" +
                "                    \"ids:fileName\": null\n" +
                "                }\n" +
                "            },\n" +
                "            \"policyId\": null,\n" +
                "            \"assetId\": null,\n" +
                "            \"provider\": \"urn:connector:provider\",\n" +
                "            \"consumer\": \"urn:connector:consumer\",\n" +
                "            \"offerStart\": null,\n" +
                "            \"offerEnd\": null,\n" +
                "            \"contractStart\": null,\n" +
                "            \"contractEnd\": null\n" +
                "                }\n" +
                "       ]\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        ContractOffersCatalogResponse mockResponse
                = mapper.readValue(contactOfferCatalogResponse, ContractOffersCatalogResponse.class);
        return mockResponse;
    }

    private ConsumerRequest getConsumerRequest() throws Exception {
        String consumerRequest = "{\n" +
                "\t\"connectorId\": \"343cdfdfd\",\n" +
                "\t\"providerUrl\": \"http: //20.218.254.172:7171/\",\n" +
                "\t\"offers\": [{\n" +
                "\t\t\t\"offerId\": \"1\",\n" +
                "\t\t\t\"assetId\": \"12345\",\n" +
                "\t\t\t\"policyId\": \"343 fdfd\"\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"policies\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"ROLE\",\n" +
                "\t\t\t\"value\": \"ADMIN\"\n" +
                "\t\t},\n" +
                "        {\n" +
                "\t\t\t\"type\": \"DURATION\",\n" +
                "\t\t\t\"value\": \"3 Day(s)\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        ConsumerRequest mockRequest
                = mapper.readValue(consumerRequest, ConsumerRequest.class);
        return mockRequest;
    }

}