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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.sde.EnablePostgreSQL;
import org.eclipse.tractusx.sde.agent.model.SchedulerConfigModel;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.retrieverl.service.SchedulerConfigService;
import org.eclipse.tractusx.sde.retrieverl.service.SftpRetrieverFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(username = "Admin", authorities = { "Admin" })
@EnablePostgreSQL
class AutoUploadAgentConfigControllerTest {

	@Autowired
	private MockMvc mvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	SftpRetrieverFactoryImpl sftpRetrieverFactory;

	@Autowired
	SchedulerConfigService schedulerConfigService;


	@Test
	void testSaveSFTPConfig() throws Exception {
		var json = objectMapper.writeValueAsString(getBodySftp());
		mvc.perform(MockMvcRequestBuilders.put("/sftp").contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isOk());
		Assertions.assertEquals(
				objectMapper.readValue(json, SftpConfigModel.class),
				sftpRetrieverFactory.getConfiguration()
		);
		mvc.perform(MockMvcRequestBuilders.get("/sftp").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(json));
	}

	@Test
	void testSaveSchedulerHourlyTypeConfig() throws Exception {
		var json = objectMapper.writeValueAsString(getSchedulerHourBody());
		mvc.perform(MockMvcRequestBuilders.put("/scheduler").contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isOk());

		Assertions.assertEquals(
				objectMapper.readValue(json, SchedulerConfigModel.class),
				schedulerConfigService.getConfiguration()
		);
		mvc.perform(MockMvcRequestBuilders.get("/scheduler").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(json));
	}


	@Test
	void testSaveSchedulerDailyTypeConfig() throws Exception {
		var json = objectMapper.writeValueAsString(getJsonSchedulerDailyBody());
		mvc.perform(MockMvcRequestBuilders.put("/scheduler").contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isOk());
		Assertions.assertEquals(
				objectMapper.readValue(json, SchedulerConfigModel.class),
				schedulerConfigService.getConfiguration()
		);
		mvc.perform(MockMvcRequestBuilders.get("/scheduler").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(json));
	}

	@Test
	void testSaveSchedulerWeeklyTypeConfig() throws Exception {
		var json = objectMapper.writeValueAsString(getJsonSchedulerWeeklyBody());
		mvc.perform(MockMvcRequestBuilders.put("/scheduler").contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isOk());
		Assertions.assertEquals(
				objectMapper.readValue(json, SchedulerConfigModel.class),
				schedulerConfigService.getConfiguration()
		);
		mvc.perform(MockMvcRequestBuilders.get("/scheduler").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(json));
	}



	private Map<String, Object> getBodySftp() {
		return Map.of(
			"host", "127.0.0.1",
			"port", 2,
			"username", "foo",
			"password", "pass",
			"toBeProcessedLocation", "/upload/sftp/tobe",
			"failedLocation", "/upload/sftp/failed",
			"partialSuccessLocation", "/upload/partial",
			"successLocation", "/upload/sftp/success",
			"inProgressLocation", "/upload/sftp/inprogress"
		);
	}

	private Map<String, Object> getSchedulerHourBody() {
		return Map.of(
				"type", "HOURLY",
				"time", "2"
		);
	}

	private Map<String, Object> getJsonSchedulerDailyBody() {
		return Map.of(
				"type", "DAILY",
				"time", "13:30"
		);
	}

	private Map<String, Object> getJsonSchedulerWeeklyBody() {
		return  Map.of(
			"type", "WEEKLY",
			"day", "1",
			"time", "13:30"
		);
	}
}
