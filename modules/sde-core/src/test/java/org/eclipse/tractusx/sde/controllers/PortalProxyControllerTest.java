/********************************************************************************
 * Copyright (c) 2022,2024 T-Systems International GmbH
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.tractusx.sde.core.controller.PortalProxyController;
import org.eclipse.tractusx.sde.core.service.PartnerPoolService;
import org.eclipse.tractusx.sde.portal.handler.PortalProxyService;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.eclipse.tractusx.sde.portal.model.response.UnifiedBPNValidationStatusEnum;
import org.eclipse.tractusx.sde.portal.model.response.UnifiedBpnValidationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = {PortalProxyController.class})
@ExtendWith(SpringExtension.class)
class PortalProxyControllerTest {

    @MockBean
    private PortalProxyService portalProxyService;
    
    @MockBean
    private PartnerPoolService partnerPoolService;

    @Autowired
    private PortalProxyController consumerController;
    
    
    @Test
    void testFetchLegalEntitiesData() throws Exception {
        when(partnerPoolService.fetchLegalEntitiesData((String) any(),(String) any(), (Integer) any(), (Integer) any()))
                .thenReturn(List.of());
        MockHttpServletRequestBuilder getResult = MockMvcRequestBuilders.get("/legal-entities");
        MockHttpServletRequestBuilder paramResult = getResult.param("page", String.valueOf(0)).param("searchText", "bmw");
        MockHttpServletRequestBuilder requestBuilder = paramResult.param("size", String.valueOf(10));
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(consumerController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(200));
    }

    @Test
    void testFetchConnectorInfo() throws Exception {
        List<ConnectorInfo> connectorInfo = List.of(ConnectorInfo.builder().bpn("Bpns").connectorEndpoint(List.of("http://localhost:8080")).build());
        when(portalProxyService.fetchConnectorInfo(List.of("Bpns"))).thenReturn(connectorInfo);
        MockHttpServletRequestBuilder contentTypeResult = MockMvcRequestBuilders.post("/connectors-discovery")
                .contentType(MediaType.APPLICATION_JSON);

        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequestBuilder requestBuilder = contentTypeResult
                .content(objectMapper.writeValueAsString(new String[]{new String()}));
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(consumerController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(200));
    }
   
    @Test
    void testGetUnifiedBPNValidateSuccess() throws Exception {
    	ObjectMapper mapper = new ObjectMapper();
    	UnifiedBpnValidationResponse response = UnifiedBpnValidationResponse.builder()
				.msg("BPNL001000TS0100 for BPN number found valid Connector in partner network")
				.bpnStatus(UnifiedBPNValidationStatusEnum.FULL_PARTNER).build();
        when(portalProxyService.unifiedBpnValidation((String) any()))
                .thenReturn(response);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/unified-bpn-validation/BPNL001000TS0100");
        MockMvcBuilders.standaloneSetup(consumerController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(response)));
    }
}
