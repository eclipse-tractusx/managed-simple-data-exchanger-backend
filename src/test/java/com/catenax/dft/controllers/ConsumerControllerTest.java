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
 *
 */

package com.catenax.dft.controllers;

import com.catenax.dft.model.response.QueryDataOfferModel;
import com.catenax.dft.service.ConsumerControlPanelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ConsumerController.class})
@ExtendWith(SpringExtension.class)
class ConsumerControllerTest {
    @MockBean
    private ConsumerControlPanelService consumerControlPanelService;

    @Autowired
    private ConsumerController consumerController;

    //@Test
    void testQueryOnDataOfferWithoutOfferModel() throws Exception {
        when(consumerControlPanelService.queryOnDataOffers((String) any())).thenReturn(new ArrayList<>());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/query-data-offers")
                .param("providerUrl", "foo");
        MockMvcBuilders.standaloneSetup(consumerController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content().string("[]"));
    }


    //@Test
    void testQueryOnDataOffersWithOfferModel() throws Exception {
        ArrayList<QueryDataOfferModel> queryDataOfferModelList = new ArrayList<>();
        queryDataOfferModelList.add(new QueryDataOfferModel());
        when(consumerControlPanelService.queryOnDataOffers((String) any())).thenReturn(queryDataOfferModelList);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/query-data-offers")
                .param("providerUrl", "foo");
        MockMvcBuilders.standaloneSetup(consumerController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.content()
                        .string(
                                "[{\"connectorOfferid\":null,\"connectorOfferUrl\":null,\"title\":null,\"version\":null,\"description\":null,"
                                        + "\"created\":null,\"modified\":null,\"publisher\":null,\"offerInfo\":null,\"typeOfAccess\":null,\"bpnNumbers\":null"
                                        + ",\"contractInfo\":null,\"usagePolicies\":null}]"));
    }
}

