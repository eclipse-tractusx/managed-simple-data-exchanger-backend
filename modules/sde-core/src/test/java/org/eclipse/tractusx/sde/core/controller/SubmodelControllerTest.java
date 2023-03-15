/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.controller;

import java.util.List;

import org.eclipse.tractusx.sde.core.registry.UsecaseRegistration;
import org.eclipse.tractusx.sde.core.service.SubmodelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {SubmodelController.class})
@ExtendWith(SpringExtension.class)
class SubmodelControllerTest {

	@MockBean
    private SubmodelService submodelService;

    @Autowired
    private SubmodelController submodelController;
    
    @MockBean
    private UsecaseRegistration usecaseRegistry;
    
    private static final List<String> USECASES_LIST = List.of("traceability",
							    					"circulareconomy",
							    					"quality",
													"sustainability");
    
    
    @Test
	void testGetAllUsecasesSuccess() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/usecases")
				.contentType(MediaType.APPLICATION_JSON);
		
		MockMvcBuilders.standaloneSetup(submodelController).build()
				.perform(requestBuilder)
				.andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
	void testGetSubmodelsSuccess() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/submodels")
				.contentType(MediaType.APPLICATION_JSON);
		
		MockMvcBuilders.standaloneSetup(submodelController).build()
				.perform(requestBuilder)
				.andExpect(MockMvcResultMatchers.status().isOk());
    }
    
	@Test
	void testGetSubmodelsWithUsecasesSuccess() throws Exception {
		for (String usecase : USECASES_LIST) {
			MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/submodels")
					.param("usecase", usecase)
					.contentType(MediaType.APPLICATION_JSON);

			MockMvcBuilders.standaloneSetup(submodelController).build().perform(requestBuilder)
					.andExpect(MockMvcResultMatchers.status().isOk());
		}
	}

}
