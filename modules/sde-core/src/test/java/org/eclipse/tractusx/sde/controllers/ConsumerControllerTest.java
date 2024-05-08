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

package org.eclipse.tractusx.sde.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.common.entities.Policies;
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
		when(consumerControlPanelService.queryOnDataOffers((String) any(), (String) any(), (String) any(), anyInt(),
				anyInt())).thenReturn(new ArrayList<>());
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/query-data-offers")
				.param("bpnNumber", "foo");

		MockMvcBuilders.standaloneSetup(consumerController).build().perform(requestBuilder)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.content().string("[]"));
	}

	@Test
	void testQueryOnDataOffersWithOfferModel() throws Exception {
		ArrayList<QueryDataOfferModel> queryDataOfferModelList = new ArrayList<>();
		queryDataOfferModelList.add(QueryDataOfferModel.builder()
				.assetId("foo")
				.connectorId("test")
				.offerId("offer")
				.build());
		when(consumerControlPanelService.queryOnDataOffers((String) any(), (String) any(), (String) any(), anyInt(),
				anyInt())).thenReturn(queryDataOfferModelList);
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/query-data-offers")
				.param("bpnNumber", "foo");

		MockMvcBuilders.standaloneSetup(consumerController).build().perform(requestBuilder)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"))
				.andExpect(MockMvcResultMatchers.content().string(
						"[{\"connectorId\":\"test\",\"assetId\":\"foo\",\"offerId\":\"offer\"}]"));

	}

	@Test
	void testSubscribeDataOffersBadRequest() throws Exception {
		doNothing().when(consumerControlPanelService).subscribeDataOffers((ConsumerRequest) any(), anyString());

		ConsumerRequest consumerRequest = ConsumerRequest.builder().offers(new ArrayList<>())
				.usagePolicies(List.of()).build();
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
		List<Policies> policies = new ArrayList<>();
		Offer mockOffer = Mockito.mock(Offer.class);
		offers.add(mockOffer);
		Policies mockPolicy = Mockito.mock(Policies.class);
		policies.add(mockPolicy);
		ConsumerRequest consumerRequest = ConsumerRequest.builder().offers(offers)
				.usagePolicies(policies).build();
		String content = (new ObjectMapper()).writeValueAsString(consumerRequest);
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/subscribe-data-offers")
				.contentType(MediaType.APPLICATION_JSON).content(content);
		MockMvcBuilders.standaloneSetup(consumerController).build().perform(requestBuilder)
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

}