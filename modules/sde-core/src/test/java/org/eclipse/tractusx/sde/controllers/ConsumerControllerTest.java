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

package org.eclipse.tractusx.sde.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.core.controller.ConsumerController;
import org.eclipse.tractusx.sde.core.service.ConsumerService;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { ConsumerController.class })
@ExtendWith(SpringExtension.class)
class ConsumerControllerTest {
	@MockBean
	private ConsumerControlPanelService consumerControlPanelService;

	@MockBean
    private ConsumerService consumerService;
    
	@Autowired
	private ConsumerController consumerController;

	@Test
	void testQueryOnDataOfferWithoutOfferModel() throws Exception {
		when(consumerControlPanelService.queryOnDataOffers((String) any(), anyInt(), anyInt(), any()))
				.thenReturn(new ArrayList<>());
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/query-data-offers")
				.param("providerUrl", "foo");
		MockMvcBuilders.standaloneSetup(consumerController).build().perform(requestBuilder)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.content().string("[]"));
	}

	@Test
	void testQueryOnDataOffersWithOfferModel() throws Exception {
		ArrayList<QueryDataOfferModel> queryDataOfferModelList = new ArrayList<>();
		queryDataOfferModelList.add(new QueryDataOfferModel());
		when(consumerControlPanelService.queryOnDataOffers((String) any(), anyInt(), anyInt(), any()))
				.thenReturn(queryDataOfferModelList);
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/query-data-offers")
				.param("providerUrl", "foo");
		MockMvcBuilders.standaloneSetup(consumerController).build().perform(requestBuilder)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.content().string(
						"[{\"connectorId\":null,\"assetId\":null,\"offerId\":null,\"connectorOfferUrl\":null,\"title\":null,\"type\":null,\"version\":null,\"description\":null,\"fileName\":null,\"fileContentType\":null,\"created\":null,\"modified\":null,\"publisher\":null,\"typeOfAccess\":null,\"bpnNumbers\":null,\"policyId\":null,\"usagePolicies\":null}]"));
	}

	@Test
	void testSubscribeDataOffersBadRequest() throws Exception {
		doNothing().when(consumerControlPanelService).subscribeDataOffers((ConsumerRequest) any(), anyString());

		ConsumerRequest consumerRequest = ConsumerRequest.builder().connectorId("42").offers(new ArrayList<>())
				.policies(Map.of()).providerUrl("\"https://example.org/example\"").build();
		String content = (new ObjectMapper()).writeValueAsString(consumerRequest);
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/subscribe-data-offers")
				.contentType(MediaType.APPLICATION_JSON).content(content);
		MockMvcBuilders.standaloneSetup(consumerController).build().perform(requestBuilder)
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	// @Test
	void testSubscribeDataOffers() throws Exception {
		doNothing().when(consumerControlPanelService).subscribeDataOffers((ConsumerRequest) any(), anyString());
		List<Offer> offers = new ArrayList<>();
		EnumMap<UsagePolicyEnum, UsagePolicies> policies = new EnumMap<>(UsagePolicyEnum.class);
		Offer mockOffer = Mockito.mock(Offer.class);
		offers.add(mockOffer);
		UsagePolicies mockPolicy = Mockito.mock(UsagePolicies.class);
		UsagePolicyEnum mockKey = Mockito.mock(UsagePolicyEnum.class);
		policies.put(mockKey, mockPolicy);
		ConsumerRequest consumerRequest = ConsumerRequest.builder().connectorId("42").offers(offers).policies(policies)
				.providerUrl("\"https://example.org/example\"").build();
		String content = (new ObjectMapper()).writeValueAsString(consumerRequest);
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/subscribe-data-offers")
				.contentType(MediaType.APPLICATION_JSON).content(content);
		MockMvcBuilders.standaloneSetup(consumerController).build().perform(requestBuilder)
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

}