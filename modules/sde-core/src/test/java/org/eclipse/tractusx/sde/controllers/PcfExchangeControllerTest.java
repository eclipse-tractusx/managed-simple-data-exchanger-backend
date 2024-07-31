/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.util.UUID;

import org.eclipse.tractusx.sde.core.controller.PcfExchangeController;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;
import org.eclipse.tractusx.sde.pcfexchange.service.IPCFExchangeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = { PcfExchangeController.class })
@ExtendWith(SpringExtension.class)
class PcfExchangeControllerTest {

	@MockBean
    private IPCFExchangeService pcfExchangeService;
    
	@Autowired
	private PcfExchangeController pcfExchangeController;
	
	
	@Test
	void testGetPcfConsumerDataSuccess() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/pcf/{type}/requests","CONSUMER")
				.param("status", "")
				.param("offset", String.valueOf(0))
				.param("maxLimit", String.valueOf(10));
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().is(200));
	}
	
	@Test
	void testGetPcfProviderDataSuccess() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/pcf/{type}/requests","PROVIDER")
				.param("status", "REQUESTED")
				.param("offset", String.valueOf(0))
				.param("maxLimit", String.valueOf(10));
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().is(200));
	}
	
	@Test
	void testGetPcfProviderDataFailure() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/pcf/{type}/requests","")
				.param("status", "REQUESTED")
				.param("offset", String.valueOf(0))
				.param("maxLimit", String.valueOf(10));
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().is(404));
	}
	
	@Test
	void testGetPcfByProductSuccess() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/pcf/productIds/{productId}","test_product")
				.header("Edc-Bpn", "BPNL001000TS0100")
				.param("requestId", UUID.randomUUID().toString())
				.param("message", "This is test request");
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().isAccepted());
	}
	
	@Test
	void testUploadPcfSubmodelSuccess() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/pcf/productIds/{productId}","test_product")
				.header("Edc-Bpn", "BPNL001000TS0100")
				.param("requestId", UUID.randomUUID().toString())
				.param("message", "This is test request")
				.contentType("application/json")
				.content(getPCFJsonResponse());
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	void testUploadPcfSubmodelSuccessForceUpdate() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/pcf/productIds/{productId}","test_product")
				.header("Edc-Bpn", "BPNL001000TS0100")
				.contentType("application/json")
				.content(getPCFJsonResponse());
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	void testUploadPcfSubmodelFailure() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/pcf/productIds/{productId}","test_product")
				.header("Edc-Bpn", "")
				.param("requestId", UUID.randomUUID().toString())
				.param("message", "This is test request");
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
	}
	
	@Test
	void testRequestForPcfDataOffer() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/pcf/request/{productId}","test_product")
				.contentType("application/json")
				.content(new ConsumerRequest().toString());
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
	}
	
	@Test
	void testViewForPcfDataOfferSuccess() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/pcf/request/{requestId}",UUID.randomUUID().toString());
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().is(200));
	}
	
	@Test
	void testActionOnPcfRequestAndSendNotificationToConsumerSuccess() throws Exception {
		
		when(pcfExchangeService.actionOnPcfRequestAndSendNotificationToConsumer(any()))
		.thenReturn(new String());
		
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/pcf/actionsonrequest")
				.contentType("application/json")
				.content(new PcfRequestModel().toString());;
		ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(pcfExchangeController).build()
				.perform(requestBuilder);
		actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
	}
	
	
	
	private String getPCFJsonResponse() {
		String bodyRequest = "{\n"
				+ "    \"row_data\": [\n"
				+ "        {\n"
				+ "            \"id\": \"3893bb5d-da16-4dc1-9185-11d97476c7a7\",\n"
				+ "            \"specVersion\": \"2.0.1-20230314\",\n"
				+ "            \"partialFullPcf\": \"Cradle-to-gate\",\n"
				+ "            \"precedingPfId\": \"3893bb5d-da16-4dc1-9185-11d97476c7b7\",\n"
				+ "            \"version\": 0,\n"
				+ "            \"created\": \"2022-05-22T21:47:32Z\",\n"
				+ "            \"extWBCSD_pfStatus\": \"Active\",\n"
				+ "            \"validityPeriodStart\": \"\",\n"
				+ "            \"validityPeriodEnd\": \"\",\n"
				+ "            \"comment\": \"Comment for version 42.\",\n"
				+ "            \"pcfLegalStatement\": \"This PCF (Product Carbon Footprint) is for information purposes only. It is based upon the standards mentioned above.\",\n"
				+ "            \"companyName\": \"My Corp\",\n"
				+ "            \"companyId\": \"urn:uuid:51131FB5-42A2-4267-A402-0ECFEFAD16A9\",\n"
				+ "            \"productDescription\": \"Ethanol, 95% solution\",\n"
				+ "            \"productId\": \"urn:gtin:47123450605077\",\n"
				+ "            \"extWBCSD_productCodeCpc\": \"011-99000\",\n"
				+ "            \"productName\": \"My Product Name\",\n"
				+ "            \"declaredUnit\": \"liter\",\n"
				+ "            \"unitaryProductAmount\": 1000.0,\n"
				+ "            \"productMassPerDeclaredUnit\": 0.456,\n"
				+ "            \"exemptedEmissionsPercent\": 0.0,\n"
				+ "            \"exemptedEmissionsDescription\": \"No exemption\",\n"
				+ "            \"extWBCSD_packagingEmissionsIncluded\": \"true\",\n"
				+ "            \"boundaryProcessesDescription\": \"Electricity consumption included as an input in the production phase\",\n"
				+ "            \"geographyCountrySubdivision\": \"US-NY\",\n"
				+ "            \"geographyCountry\": \"DE\",\n"
				+ "            \"geographyRegionOrSubregion\": \"Africa\",\n"
				+ "            \"referencePeriodStart\": \"2022-01-01T00:00:01Z\",\n"
				+ "            \"referencePeriodEnd\": \"2022-12-31T23:59:59Z\",\n"
				+ "            \"crossSectoralStandard\": \"GHG Protocol Product standard\",\n"
				+ "            \"extWBCSD_operator\": \"PEF\",\n"
				+ "            \"ruleName\": \"urn:tfs-initiative.com:PCR:The Product Carbon Footprint Guideline for the Chemical Industry:version:v2.0\",\n"
				+ "            \"extWBCSD_otherOperatorName\": \"NSF\",\n"
				+ "            \"extWBCSD_characterizationFactors\": \"AR5\",\n"
				+ "            \"extWBCSD_allocationRulesDescription\": \"In accordance with Catena-X PCF Rulebook\",\n"
				+ "            \"extTFS_allocationWasteIncineration\": \"cut-off\",\n"
				+ "            \"primaryDataShare\": 56.12,\n"
				+ "            \"secondaryEmissionFactorSource\": \"ecoinvent 3.8\",\n"
				+ "            \"coveragePercent\": 100,\n"
				+ "            \"technologicalDQR\": 2.0,\n"
				+ "            \"temporalDQR\": 2.0,\n"
				+ "            \"geographicalDQR\": 2.0,\n"
				+ "            \"completenessDQR\": 2.0,\n"
				+ "            \"reliabilityDQR\": 2.0,\n"
				+ "            \"pcfExcludingBiogenic\": 2.0,\n"
				+ "            \"pcfIncludingBiogenic\": 1.0,\n"
				+ "            \"fossilGhgEmissions\": 0.5,\n"
				+ "            \"biogenicCarbonEmissionsOtherThanCO2\": 1.0,\n"
				+ "            \"biogenicCarbonWithdrawal\": 0.0,\n"
				+ "            \"dlucGhgEmissions\": 0.4,\n"
				+ "            \"extTFS_luGhgEmissions\": 0.3,\n"
				+ "            \"aircraftGhgEmissions\": 0.0,\n"
				+ "            \"extWBCSD_packagingGhgEmissions\": 0,\n"
				+ "            \"distributionStagePcfExcludingBiogenic\": 1.5,\n"
				+ "            \"distributionStagePcfIncludingBiogenic\": 0.0,\n"
				+ "            \"distributionStageFossilGhgEmissions\": 0.5,\n"
				+ "            \"distributionStageBiogenicCarbonEmissionsOtherThanCO2\": 1.0,\n"
				+ "            \"distributionStageBiogenicCarbonWithdrawal\": 0.5,\n"
				+ "            \"extTFS_distributionStageDlucGhgEmissions\": 1.0,\n"
				+ "            \"extTFS_distributionStageLuGhgEmissions\": 1.1,\n"
				+ "            \"carbonContentTotal\": 2.5,\n"
				+ "            \"extWBCSD_fossilCarbonContent\": 0.1,\n"
				+ "            \"carbonContentBiogenic\": 0.0,\n"
				+ "            \"assetLifeCyclePhase\": \"AsPlanned\"\n"
				+ "        }\n"
				+ "    ],\n"
				+ "    \"access_policies\": [\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"BusinessPartnerNumber\",\n"
				+ "            \"value\": [\n"
				+ "                \"BPNL001000TS0100\"\n"
				+ "            ]\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"Membership\",\n"
				+ "            \"value\": [\n"
				+ "                \"active\"\n"
				+ "            ]\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"companyRole.dismantler\",\n"
				+ "            \"value\": [\n"
				+ "                \"active\"\n"
				+ "            ]\n"
				+ "        }\n"
				+ "    ],\n"
				+ "    \"usage_policies\": [\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"Membership\",\n"
				+ "            \"value\": [\n"
				+ "                \"active\"\n"
				+ "            ]\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"companyRole.dismantler\",\n"
				+ "            \"value\": [\n"
				+ "                \"active\"\n"
				+ "            ]\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"FrameworkAgreement.pcf\",\n"
				+ "            \"value\": [\n"
				+ "                \"active:v1.0.0\"\n"
				+ "            ]\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"FrameworkAgreement.sustainability\",\n"
				+ "            \"value\": [\n"
				+ "                \"active:v1.0.0\"\n"
				+ "            ]\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"PURPOSE\",\n"
				+ "            \"value\": []\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"PURPOSE\",\n"
				+ "            \"value\": []\n"
				+ "        },\n"
				+ "        {\n"
				+ "            \"technicalKey\": \"CUSTOM\",\n"
				+ "            \"value\": []\n"
				+ "        }\n"
				+ "    ]\n"
				+ "}";
		
		return bodyRequest;
	}
	
}
