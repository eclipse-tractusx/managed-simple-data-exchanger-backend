/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.edc.api.ContractOfferCatalogApi;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.facilitator.ContractNegotiateManagementHelper;
import org.eclipse.tractusx.sde.edc.gateways.database.ContractNegotiationInfoRepository;
import org.eclipse.tractusx.sde.edc.model.contractoffers.ContractOfferRequestFactory;
import org.eclipse.tractusx.sde.edc.model.contractoffers.ContractOffersCatalogResponse;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.OfferRequest;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.eclipse.tractusx.sde.portal.api.IPartnerPoolExternalServiceApi;
import org.eclipse.tractusx.sde.portal.api.IPortalExternalServiceApi;
import org.eclipse.tractusx.sde.portal.utils.TokenUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { ConsumerControlPanelService.class, String.class })
@ExtendWith(SpringExtension.class)
class ConsumerControlPanelServiceTest {
	@MockBean
	private IPortalExternalServiceApi connectorDiscoveryApi;

	@MockBean
	private TokenUtility keycloakUtil;

	@MockBean
	private IPartnerPoolExternalServiceApi legalEntityDataApi;

	@MockBean
	private ContractNegotiateManagementHelper contractNegotiateManagement;

	@MockBean
	private ContractNegotiationInfoRepository contractNegotiationInfoRepository;

	@Autowired
	private ConsumerControlPanelService consumerControlPanelService;

	@MockBean
	private ContractOfferCatalogApi contractOfferCatalogApi;

	@MockBean
	private PolicyConstraintBuilderService policyConstraintBuilderService;
	
	@MockBean
	private ContractOfferRequestFactory contractOfferRequestFactory;

	// @Test
	void testQueryOnDataOfferEmpty() throws Exception {
		JsonNode contractOffersCatalogResponse = null;
		when(contractOfferCatalogApi.getContractOffersCatalog((JsonNode) any()))
				.thenReturn(contractOffersCatalogResponse);
		assertTrue(consumerControlPanelService.queryOnDataOffers("https://example.org/example", anyInt(), anyInt())
				.isEmpty());
		verify(contractOfferCatalogApi).getContractOffersCatalog((JsonNode) any());
	}

	// @Test
	void testQueryOnDataOffersWithUsagePolicies() throws Exception {

		JsonNode contractOffersCatalogResponse = null;
		when(contractOfferCatalogApi.getContractOffersCatalog((JsonNode) any()))
				.thenReturn(contractOffersCatalogResponse);
		assertEquals(1, consumerControlPanelService.queryOnDataOffers("https://example.org/example", anyInt(), anyInt())
				.size());
		verify(contractOfferCatalogApi).getContractOffersCatalog((JsonNode) any());
	}

	// @Test
	void testQueryOnDataOffersWithMissingUsagePolicies() throws Exception {

		JsonNode contractOffersCatalogResponse = null;
		when(contractOfferCatalogApi.getContractOffersCatalog((JsonNode) any()))
				.thenReturn(contractOffersCatalogResponse);
		assertEquals(1, consumerControlPanelService.queryOnDataOffers("https://example.org/example", anyInt(), anyInt())
				.size());
		verify(contractOfferCatalogApi).getContractOffersCatalog((JsonNode) any());
	}

	//@Test
	void testSubscribeDataOffers1() {
		ArrayList<OfferRequest> offerRequestList = new ArrayList<>();
		List<UsagePolicies> usagePolicies = new ArrayList<>();
		UsagePolicies usagePolicy = UsagePolicies.builder().type(UsagePolicyEnum.CUSTOM).value("Sample")
				.typeOfAccess(PolicyAccessEnum.RESTRICTED).build();
		usagePolicies.add(usagePolicy);
		ConsumerRequest consumerRequest = new ConsumerRequest("42", "https://example.org/example", offerRequestList,
				usagePolicies);
		String processId = UUID.randomUUID().toString();
		consumerControlPanelService.subscribeDataOffers(consumerRequest, processId);
		assertEquals("42", consumerRequest.getConnectorId());
		assertEquals("https://example.org/example", consumerRequest.getProviderUrl());
		List<UsagePolicies> policies = consumerRequest.getPolicies();
		ActionRequest list = ActionRequest.builder().build();
		ConstraintRequest constraintRequest = ConstraintRequest.builder().leftOperand(any()).rightOperand(any())
				.operator("EQ").build();
		list.addProperty("odrl:and", constraintRequest);
		when(policyConstraintBuilderService.getUsagePolicyConstraints(any())).thenReturn(list);
		assertEquals(usagePolicies, policies);
		assertEquals(1, consumerControlPanelService.getAuthHeader().size());
	}

	//@Test
	void testSubscribeDataOffers2() {
		List<UsagePolicies> usagePolicies = new ArrayList<>();
		UsagePolicies usagePolicy = UsagePolicies.builder().type(UsagePolicyEnum.CUSTOM).value("Sample")
				.typeOfAccess(PolicyAccessEnum.RESTRICTED).build();
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
		String contactOfferCatalogResponse = "{\n" + "    \"id\": \"default\",\n" + "    \"contractOffers\": [ "
				+ "       {\n"
				+ "            \"id\": \"5ff29ec1-90ae-4e75-adf1-afb19ed8c686:cf0fc8c3-c7d6-4b52-baac-764f62312778\",\n"
				+ "            \"policy\": {\n" + "                \"uid\": \"5dc869fc-9b30-4622-8e25-310033b3b927\",\n"
				+ "                \"permissions\": [\n" + "                    {\n"
				+ "                        \"edctype\": \"dataspaceconnector:permission\",\n"
				+ "                        \"uid\": null,\n"
				+ "                        \"target\": \"urn:uuid:4cfaf444-ba35-4971-9b02-162a1c8cdfe5-urn:uuid:7b5a2bfc-36ae-49b3-a7a1-c3bb6cfed190\",\n"
				+ "                        \"action\": {\n" + "                            \"type\": \"USE\",\n"
				+ "                            \"includedIn\": null,\n"
				+ "                            \"constraint\": null\n" + "                        },\n"
				+ "                        \"assignee\": null,\n" + "                        \"assigner\": null,\n"
				+ "                        \"constraints\": [\n" + "                            {\n"
				+ "                                \"edctype\": \"AtomicConstraint\",\n"
				+ "                                \"leftExpression\": {\n"
				+ "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n"
				+ "                                    \"value\": \"BusinessPartnerNumber\"\n"
				+ "                                },\n" + "                                \"rightExpression\": {\n"
				+ "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n"
				+ "                                    \"value\": \"[BPNL00000005CONS, BPNL00000005PROV]\"\n"
				+ "                                },\n" + "                                \"operator\": \"IN\"\n"
				+ "                            },\n" + "                            {\n"
				+ "                                \"edctype\": \"AtomicConstraint\",\n"
				+ "                                \"leftExpression\": {\n"
				+ "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n"
				+ "                                    \"value\": \"idsc:ELAPSED_TIME\"\n"
				+ "                                },\n" + "                                \"rightExpression\": {\n"
				+ "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n"
				+ "                                    \"value\": \"P0Y0M3DT00H00M00S\"\n"
				+ "                                },\n" + "                                \"operator\": \"LEQ\"\n"
				+ "                            },\n" + "                            {\n"
				+ "                                \"edctype\": \"AtomicConstraint\",\n"
				+ "                                \"leftExpression\": {\n"
				+ "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n"
				+ "                                    \"value\": \"idsc:ROLE\"\n"
				+ "                                },\n" + "                                \"rightExpression\": {\n"
				+ "                                    \"edctype\": \"dataspaceconnector:literalexpression\",\n"
				+ "                                    \"value\": \"ADMIN\"\n" + "                                },\n"
				+ "                                \"operator\": \"EQ\"\n" + "                            }\n"
				+ "                        ],\n" + "                        \"duties\": []\n"
				+ "                    }\n" + "                ],\n" + "                \"prohibitions\": [],\n"
				+ "                \"obligations\": [],\n" + "                \"extensibleProperties\": {},\n"
				+ "                \"inheritsFrom\": null,\n" + "                \"assigner\": null,\n"
				+ "                \"assignee\": null,\n" + "                \"target\": null,\n"
				+ "                \"@type\": {\n" + "                    \"@policytype\": \"set\"\n"
				+ "                }\n" + "            },\n" + "            \"asset\": {\n"
				+ "                \"properties\": {\n"
				+ "                    \"asset:prop:name\": \"Serialized Part - Submodel SerialPartTypization\",\n"
				+ "                    \"asset:prop:contenttype\": \"application/json\",\n"
				+ "                    \"asset:prop:description\": \"Serialized Part - Submodel SerialPartTypization\",\n"
				+ "                    \"ids:byteSize\": null,\n"
				+ "                    \"asset:prop:version\": \"1.0.0\",\n"
				+ "                    \"asset:prop:id\": \"urn:uuid:4cfaf444-ba35-4971-9b02-162a1c8cdfe5-urn:uuid:7b5a2bfc-36ae-49b3-a7a1-c3bb6cfed190\",\n"
				+ "                    \"ids:fileName\": null\n" + "                }\n" + "            },\n"
				+ "            \"policyId\": null,\n" + "            \"assetId\": null,\n"
				+ "            \"provider\": \"urn:connector:provider\",\n"
				+ "            \"consumer\": \"urn:connector:consumer\",\n" + "            \"offerStart\": null,\n"
				+ "            \"offerEnd\": null,\n" + "            \"contractStart\": null,\n"
				+ "            \"contractEnd\": null\n" + "                }\n" + "       ]\n" + "}";
		ObjectMapper mapper = new ObjectMapper();
		ContractOffersCatalogResponse mockResponse = mapper.readValue(contactOfferCatalogResponse,
				ContractOffersCatalogResponse.class);
		return mockResponse;
	}

	private ContractOffersCatalogResponse getCatalogObjectWithMissingConstraints() throws Exception {
		String contactOfferCatalogResponse = "{\n" + "    \"id\": \"default\",\n" + "    \"contractOffers\": [ "
				+ "       {\n"
				+ "            \"id\": \"5ff29ec1-90ae-4e75-adf1-afb19ed8c686:cf0fc8c3-c7d6-4b52-baac-764f62312778\",\n"
				+ "            \"policy\": {\n" + "                \"uid\": \"5dc869fc-9b30-4622-8e25-310033b3b927\",\n"
				+ "                \"permissions\": [\n" + "                    {\n"
				+ "                        \"edctype\": \"dataspaceconnector:permission\",\n"
				+ "                        \"uid\": null,\n"
				+ "                        \"target\": \"urn:uuid:4cfaf444-ba35-4971-9b02-162a1c8cdfe5-urn:uuid:7b5a2bfc-36ae-49b3-a7a1-c3bb6cfed190\",\n"
				+ "                        \"action\": {\n" + "                            \"type\": \"USE\",\n"
				+ "                            \"includedIn\": null,\n"
				+ "                            \"constraint\": null\n" + "                        },\n"
				+ "                        \"assignee\": null,\n" + "                        \"assigner\": null,\n"
				+ "                        \"constraints\": [],\n" + "                        \"duties\": []\n"
				+ "                    }\n" + "                ],\n" + "                \"prohibitions\": [],\n"
				+ "                \"obligations\": [],\n" + "                \"extensibleProperties\": {},\n"
				+ "                \"inheritsFrom\": null,\n" + "                \"assigner\": null,\n"
				+ "                \"assignee\": null,\n" + "                \"target\": null,\n"
				+ "                \"@type\": {\n" + "                    \"@policytype\": \"set\"\n"
				+ "                }\n" + "            },\n" + "            \"asset\": {\n"
				+ "                \"properties\": {\n"
				+ "                    \"asset:prop:name\": \"Serialized Part - Submodel SerialPartTypization\",\n"
				+ "                    \"asset:prop:contenttype\": \"application/json\",\n"
				+ "                    \"asset:prop:description\": \"Serialized Part - Submodel SerialPartTypization\",\n"
				+ "                    \"ids:byteSize\": null,\n"
				+ "                    \"asset:prop:version\": \"1.0.0\",\n"
				+ "                    \"asset:prop:id\": \"urn:uuid:4cfaf444-ba35-4971-9b02-162a1c8cdfe5-urn:uuid:7b5a2bfc-36ae-49b3-a7a1-c3bb6cfed190\",\n"
				+ "                    \"ids:fileName\": null\n" + "                }\n" + "            },\n"
				+ "            \"policyId\": null,\n" + "            \"assetId\": null,\n"
				+ "            \"provider\": \"urn:connector:provider\",\n"
				+ "            \"consumer\": \"urn:connector:consumer\",\n" + "            \"offerStart\": null,\n"
				+ "            \"offerEnd\": null,\n" + "            \"contractStart\": null,\n"
				+ "            \"contractEnd\": null\n" + "                }\n" + "       ]\n" + "}";
		ObjectMapper mapper = new ObjectMapper();
		ContractOffersCatalogResponse mockResponse = mapper.readValue(contactOfferCatalogResponse,
				ContractOffersCatalogResponse.class);
		return mockResponse;
	}

}