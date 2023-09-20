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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.common.utils.TokenUtility;
import org.eclipse.tractusx.sde.core.service.ConsumerService;
import org.eclipse.tractusx.sde.edc.api.ContractOfferCatalogApi;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.Operator;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.facilitator.ContractNegotiateManagementHelper;
import org.eclipse.tractusx.sde.edc.facilitator.EDRRequestHelper;
import org.eclipse.tractusx.sde.edc.gateways.database.ContractNegotiationInfoRepository;
import org.eclipse.tractusx.sde.edc.model.contractoffers.ContractOfferRequestFactory;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.eclipse.tractusx.sde.portal.api.IPartnerPoolExternalServiceApi;
import org.eclipse.tractusx.sde.portal.api.IPortalExternalServiceApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
    private ConsumerService consumerService;
	
	@MockBean
	private EDRRequestHelper eDRRequestHelper;

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

	@Test
	void testQueryOnDataOfferEmpty() throws Exception {

		JsonNode json = getCatalogEmptyResponse();

		when(contractOfferCatalogApi.getContractOffersCatalog((JsonNode) any())).thenReturn(json);

		List<QueryDataOfferModel> queryOnDataOffers = consumerControlPanelService
				.queryOnDataOffers("https://example.org/example", 0, 0, null);
		assertTrue(queryOnDataOffers.isEmpty());
		verify(contractOfferCatalogApi).getContractOffersCatalog((JsonNode) any());
	}

	

	@Test
	void testQueryOnDataOffersWithUsagePolicies() throws Exception {
		
		String filterExpression = String.format("""
				 "filterExpression": [{
				    "operandLeft": "https://w3id.org/edc/v0.0.1/ns/type",
				    "operator": "=",
				    "operandRight": "data.core.digitalTwinRegistry"
				}]""");
		
		JsonNode contractOffersCatalogResponse = getCatalogResponse();
		when(contractOfferCatalogApi.getContractOffersCatalog((JsonNode) any()))
				.thenReturn(contractOffersCatalogResponse);
		assertEquals(1, consumerControlPanelService
				.queryOnDataOffers("https://example.org/example", 0, 1, filterExpression).size());
		verify(contractOfferCatalogApi).getContractOffersCatalog((JsonNode) any());
	}


	@Test
	void testSubscribeDataOffers1() {
		ArrayList<Offer> offerRequestList = new ArrayList<>();
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
		ConstraintRequest constraintRequest = ConstraintRequest.builder().leftOperand("A")
				.rightOperand("A")
				.operator(Operator.builder().id("odrl:eq").build())
				.build();
		list.addProperty("odrl:and", constraintRequest);
		when(policyConstraintBuilderService.getUsagePolicyConstraints(any())).thenReturn(list);
		assertEquals(usagePolicies, policies);
		assertEquals(1, consumerControlPanelService.getAuthHeader().size());
	}


	private JsonNode getCatalogResponse() throws JsonProcessingException, JsonMappingException {
		String resBody = String
				.format("""
							{
						    "@id": "c6b93e69-4894-4353-85bd-3f4ce097af99",
						    "@type": "dcat:Catalog",
						    "dcat:dataset": {
						        "@id": "c14cbd4d-41b5-43fb-9629-d914e478ba73",
						        "@type": "dcat:Dataset",
						        "odrl:hasPolicy": {
						            "@id": "d694c7b4-17ec-4540-9991-9cc67eeb4c99:urn:uuid:40598c0f-8c53-4aa3-8281-1d82c9299bc3-urn:uuid:df0013c4-8296-41fa-a8b9-f251e5e1f638:29c448d4-a931-42f5-8605-b00608daee7a",
						            "@type": "odrl:Set",
						            "odrl:permission": {
						                "odrl:target": "urn_uuid_40598c0f-8c53-4aa3-8281-1d82c9299bc3-urn_uuid_df0013c4-8296-41fa-a8b9-f251e5e1f638",
						                "odrl:action": {
						                    "odrl:type": "USE"
						                },
						                "odrl:constraint": {
						                    "odrl:or": {
						                        "odrl:leftOperand": "BusinessPartnerNumber",
						                        "odrl:operator": {"@id": "odrl:eq"},
						                        "odrl:rightOperand": "BPNL001000TS0100"
						                    }
						                }
						            },
						            "odrl:prohibition": [],
						            "odrl:obligation": [],
						            "odrl:target": "urn_uuid_40598c0f-8c53-4aa3-8281-1d82c9299bc3-urn_uuid_df0013c4-8296-41fa-a8b9-f251e5e1f638"
						        },
						        "dcat:distribution": [
						            {
						                "@type": "dcat:Distribution",
						                "dct:format": {
						                    "@id": "HttpProxy"
						                },
						                "dcat:accessService": "db548bb3-3341-4ae3-8d70-6d0cd4482570"
						            },
						            {
						                "@type": "dcat:Distribution",
						                "dct:format": {
						                    "@id": "AmazonS3"
						                },
						                "dcat:accessService": "db548bb3-3341-4ae3-8d70-6d0cd4482570"
						            }
						        ],
						        "edc:modified": "26/06/2023 16:27:38",
						        "edc:version": "1.0.0",
						        "edc:publisher": "BPNL001000TS0100:https://tsyste-f783465e-us.local.cx.dih-cloud.com",
						        "edc:type": "data.core.digitalTwin.submodel",
						        "edc:creationDate": "26/06/2023 16:27:38",
						        "edc:policy-id": "use-eu",
						        "edc:name": "BoM As-Built - Submodel SerialPartTypization",
						        "edc:description": "BoM As-Built - Submodel SerialPartTypization",
						        "edc:id": "urn_uuid_40598c0f-8c53-4aa3-8281-1d82c9299bc3-urn_uuid_df0013c4-8296-41fa-a8b9-f251e5e1f638",
						        "edc:contenttype": "application/json"
						    },
						    "dcat:service": {
						        "@id": "db548bb3-3341-4ae3-8d70-6d0cd4482570",
						        "@type": "dcat:DataService",
						        "dct:terms": "connector",
						        "dct:endpointUrl": "https://tsyste-f783465e-us.local.cx.dih-cloud.com/api/v1/dsp"
						    },
						    "edc:participantId": "BPNL001000TS0100",
						    "@context": {
						        "dct": "https://purl.org/dc/terms/",
						        "tx": "https://w3id.org/tractusx/v0.0.1/ns/",
						        "edc": "https://w3id.org/edc/v0.0.1/ns/",
						        "dcat": "https://www.w3.org/ns/dcat/",
						        "odrl": "http://www.w3.org/ns/odrl/2/",
						        "dspace": "https://w3id.org/dspace/v0.8/"
						    }
						}
														""");
		JsonNode json = (JsonNode) new ObjectMapper().readTree(resBody);
		return json;
	}
	
	private JsonNode getCatalogEmptyResponse() throws JsonProcessingException, JsonMappingException {
		String resBody = String
				.format("""
							{
						    "@id": "c6b93e69-4894-4353-85bd-3f4ce097af99",
						    "@type": "dcat:Catalog",
						    "dcat:dataset": [],
						    "dcat:service": {
						        "@id": "db548bb3-3341-4ae3-8d70-6d0cd4482570",
						        "@type": "dcat:DataService",
						        "dct:terms": "connector",
						        "dct:endpointUrl": "https://tsyste-f783465e-us.local.cx.dih-cloud.com/api/v1/dsp"
						    },
						    "edc:participantId": "BPNL001000TS0100",
						    "@context": {
						        "dct": "https://purl.org/dc/terms/",
						        "tx": "https://w3id.org/tractusx/v0.0.1/ns/",
						        "edc": "https://w3id.org/edc/v0.0.1/ns/",
						        "dcat": "https://www.w3.org/ns/dcat/",
						        "odrl": "http://www.w3.org/ns/odrl/2/",
						        "dspace": "https://w3id.org/dspace/v0.8/"
						    }
						}
														""");
		JsonNode json = (JsonNode) new ObjectMapper().readTree(resBody);
		return json;
	}
}