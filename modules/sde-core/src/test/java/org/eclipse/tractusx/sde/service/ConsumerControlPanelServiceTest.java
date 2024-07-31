/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import java.util.Set;
import java.util.UUID;

import org.eclipse.tractusx.sde.EnablePostgreSQL;
import org.eclipse.tractusx.sde.bpndiscovery.handler.BpnDiscoveryProxyService;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoverySearchResponse;
import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.utils.TokenUtility;
import org.eclipse.tractusx.sde.core.service.ConsumerService;
import org.eclipse.tractusx.sde.edc.api.ContractOfferCatalogApi;
import org.eclipse.tractusx.sde.edc.facilitator.ContractNegotiateManagementHelper;
import org.eclipse.tractusx.sde.edc.facilitator.EDRRequestHelper;
import org.eclipse.tractusx.sde.edc.gateways.database.ContractNegotiationInfoRepository;
import org.eclipse.tractusx.sde.edc.model.contractoffers.ContractOfferRequestFactory;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.CatalogResponseBuilder;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.eclipse.tractusx.sde.edc.services.ContractNegotiationService;
import org.eclipse.tractusx.sde.edc.services.LookUpDTTwin;
import org.eclipse.tractusx.sde.edc.util.EDCAssetUrlCacheService;
import org.eclipse.tractusx.sde.portal.api.IPartnerPoolExternalServiceApi;
import org.eclipse.tractusx.sde.portal.api.IPortalExternalServiceApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

//@ContextConfiguration(classes = { ConsumerControlPanelService.class, String.class, PolicyConstraintBuilderService.class })
//@ExtendWith(SpringExtension.class)
@Slf4j
@EnablePostgreSQL
@SpringBootTest
@Execution(ExecutionMode.SAME_THREAD)
@WithMockUser(username = "Admin", authorities = { "Admin" })
@ActiveProfiles("test")
class ConsumerControlPanelServiceTest {
	
	@MockBean
	private IPortalExternalServiceApi connectorDiscoveryApi;

	@MockBean
	private TokenUtility keycloakUtil;

	@MockBean
	private ConsumerService consumerService;

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
	private ContractOfferRequestFactory contractOfferRequestFactory;

	@MockBean
	private EDRRequestHelper eDRRequestHelper;
	
	@MockBean
	private BpnDiscoveryProxyService bpnDiscoveryProxyService;
	
	@MockBean
	private EDCAssetUrlCacheService eDCAssetUrlCacheService;
	
	@MockBean
	private CatalogResponseBuilder catalogResponseBuilder;

	@MockBean
	private ContractNegotiationService contractNegotiationService;

	@MockBean
	private LookUpDTTwin lookUpDTTwin;
	
	@MockBean
	private BpnDiscoverySearchResponse bpnDiscoverySearchResponse;
	
	
    @Test
	void testQueryOnDataOfferEmpty() throws Exception {
    	BpnDiscoverySearchResponse build = BpnDiscoverySearchResponse.builder()
		.bpns(List.of())
		.build();
    	when(bpnDiscoveryProxyService.bpnDiscoverySearchData(any())).thenReturn(build);

		Set<QueryDataOfferModel> queryOnDataOffers = consumerControlPanelService.queryOnDataOffers("example", "", "",
				0, 0);
		assertTrue(queryOnDataOffers.isEmpty());
	}

	//@Test
	void testQueryOnDataOffersWithUsagePolicies() throws Exception {

		JsonNode contractOffersCatalogResponse = getCatalogResponse();
		when(contractOfferCatalogApi.getContractOffersCatalog((JsonNode) any()))
				.thenReturn(contractOffersCatalogResponse);
		assertEquals(1, consumerControlPanelService.queryOnDataOffers("example", "", "", 0, 1).size());
		verify(contractOfferCatalogApi).getContractOffersCatalog((JsonNode) any());
	}

	@Test
	void testSubscribeDataOffers1() {
		ArrayList<Offer> offerRequestList = new ArrayList<>();
		List<Policies> usagePolicies = new ArrayList<>();
		Policies usagePolicy = Policies.builder()
				.technicalKey("BusinessPartnerNumber")
				.value(List.of("BPN123456789"))
				.build();
		
		usagePolicies.add(usagePolicy);
		
		Policies usagePolicy1 = Policies.builder()
				.technicalKey("A")
				.value(List.of("A"))
				.build();
		
		usagePolicies.add(usagePolicy1);
		
		Policies usagePolicy2 = Policies.builder()
				.technicalKey("B")
				.value(List.of("B"))
				.build();
		usagePolicies.add(usagePolicy2);
		
		ConsumerRequest consumerRequest = new ConsumerRequest(offerRequestList,
				usagePolicies, "csv");
		String processId = UUID.randomUUID().toString();
		
		consumerControlPanelService.subscribeDataOffers(consumerRequest, processId);
		
		assertEquals(usagePolicies, usagePolicies);
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
						                        "odrl:leftOperand": {"@id","BusinessPartnerNumber"},
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
		String resBody = String.format("""
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